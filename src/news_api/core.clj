(ns news-api.core
  (:require [clojure [string :refer [blank?]]
                     [zip :as zip]]
            [clojure.core.cache :as cache]
            [resourceful :refer [resource]]
            [compojure [core :refer [GET POST routes]]
                       [route :refer [not-found]]]
            [schema.core :as s]
            [environ.core :refer [env]]
            [clj-http.client :as client]
            [clojure.data.xml :refer [element] :as xml]
            [ring.util.response :refer [charset content-type created get-header]]
            [ring.adapter.jetty :as rj]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def api-keys #{"13tm31n"})
(def users (ref []))
(def weather-cache (atom (cache/ttl-cache-factory {} :ttl 300000)))
(def news-cache (atom (cache/ttl-cache-factory {} :ttl 300000)))

(defn save-new-user
  "Uses STM to save a new user and return the ID of the new user without
   worrying about race conditions."
  [user]
  (dosync
    (alter users conj user)
    (count @users)))

(defn error-response [code message]
  {:status code
   :headers {"Content-Type" "text/plain;charset=UTF-8"}
   :body message})

(def NonEmptyString (s/both s/Str (s/pred seq)))

(def check-user
  (s/checker
    {:name NonEmptyString
     :phone NonEmptyString
     :address {:zip #"\d{5}"
             s/Str s/Any}}))

(defn add-links-to-user
  "Given a user map and its ID, add a :links key to the map and return it"
  [user id]
  (assoc user :links {:news (str "http://localhost:5000/users/" id "/news")}))

(def users-collection
  (resource "collection of users"
    "/users"
    (POST
      {headers :headers
       {:keys [name address phone] :as user} :body
       :as req}
      (cond
        (nil? (get-header req "Content-Type"))
        (error-response 400 "The request must include the header Content-Type.")

        (not (.startsWith (get-header req "Content-Type") "application/json"))
        (error-response 415 "The request representation must be of type application/json.")

        (nil? (get-header req "Content-Length"))
        (error-response 411 "The request must include the header Content-Length.")

        (some? (check-user user))
        (let [error-message (check-user user)]  ; TODO: it’s a little silly to validate twice
          (error-response 400 (str "Request representation failed validation:\n\n" error-message)))

        :default
        (let [new-user-id (save-new-user user)
              new-resource-url (str "http://localhost:5000/users/" new-user-id)
              user-with-links (add-links-to-user user new-user-id)]
          (-> (created new-resource-url user-with-links)
              (content-type "application/json")
              (charset "UTF-8")))))))

(defn user-to-xml [user]
  (element :user {}
    (element :name {} (:name user))
    (element :phone {} (:phone user))
    (element :address {}
      (element :zip {} (get-in user [:address :zip])))
    (element :links {}
      (element :news {} (get-in user [:links :news])))))

(def a-user
  (resource "a user"
    "/users/:id"
    (GET
      {headers :headers
       {:keys [id]} :params
       :as req}
      (let [user-id (Integer/parseInt id) ; TODO: return a 404 if this fails
            user-index (dec user-id) ; because the users vector is 0-indexed
            user (get @users user-index)
            user-with-links (add-links-to-user user user-id)]
        (cond
          (nil? user)
          (error-response 404 "No such resource.")

          (and (not (blank? (get-header req "Accept")))
               (not (.startsWith (get-header req "Accept") "application/json"))
               (not (.startsWith (get-header req "Accept") "application/xml")))
          (error-response 406 "This resource supports only application/json or application/xml.")

          (.startsWith (get-header req "Accept") "application/xml")
          {:status 200
           :headers {"Content-Type" "application/xml;charset=UTF-8"}
           :body (xml/emit-str (user-to-xml user-with-links))}

          :default
          {:status 200
           :headers {"Content-Type" "application/json;charset=UTF-8"}
           :body user-with-links})))))

(defn get-coords [zip]
  (let [uri (str "http://maps.googleapis.com/maps/api/geocode/json?address=" zip)
        response (client/get uri {:accept :json, :as :json-strict})]
    (get-in response [:body :results 0 :geometry :location])))

(def get-coords-m (memoize get-coords))

(defn get-forecast [zip]
  (let [{:keys [lat lng]} (get-coords-m zip)
        base-uri "https://api.forecast.io/forecast/"
        api-key (env :forecast-key)
        uri (str base-uri api-key "/" lat "," lng)
        response (client/get uri {:accept :json, :as :json-strict})]
    (:body response)))

(defn get-forecast-cached [zip]
  (if (cache/has? @weather-cache zip)
      (swap! weather-cache #(cache/hit % zip))
      (swap! weather-cache #(cache/miss % zip (get-forecast zip))))
  (cache/lookup @weather-cache zip))

(defn get-headlines [zip]
  ;; TODO: These are specifically relevant to the provided zipcode but so far I haven’t found an
  ;; API that provides such data
  (let [api-key (env :usatoday-articles-key)
        uri (str "http://api.usatoday.com/open/articles/topnews?encoding=json&api_key=" api-key)
        response (client/get uri {:accept :json, :as :json-strict})
        stories (get-in response [:body :stories])]
    (map :description stories)))

(defn get-headlines-cached [zip]
  (if (cache/has? @news-cache :headlines)
      (swap! news-cache #(cache/hit % :headlines))
      (swap! news-cache #(cache/miss % :headlines (get-headlines zip))))
  (cache/lookup @news-cache :headlines))

(def user-news
  (resource "the news for a user"
    "/users/:id/news"
    (GET
      {headers :headers
       {:keys [id]} :params
       :as req}
      (let [user-id (Integer/parseInt id) ; TODO: return a 404 if this fails
            user-index (dec user-id) ; because the users vector is 0-indexed
            user (get @users user-index)
            zip (get-in user [:address :zip])]
        (cond
          (nil? user)
          (error-response 404 "No such resource.")

          (and (not (blank? (get-header req "Accept")))
               (not (.startsWith (get-header req "Accept") "application/json")))
          (error-response 406 "This resource supports only application/json.")

          :default
          {:status 200
           :headers {"Content-Type" "application/json;charset=UTF-8"}
           :body {:weather {:forecast (get-forecast-cached zip)}
                  :news {:headlines (get-headlines-cached zip)}}})))))

(defn wrap-authentication [handler]
  (fn [request]
    (cond
      (nil? (get-header request "API-Key"))
      (error-response 401 "The request header API-Key is required.")

      (not (contains? api-keys (get-header request "API-Key")))
      (error-response 403 "You are not allowed to access this resource.")

      :default
      (handler request))))

(def ring-handler
  "this is a var so it can be used by lein-ring"
  (-> (routes users-collection
              a-user
              user-news)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      (wrap-json-response {:pretty true})
      wrap-authentication))

(defn start []
  (println "starting web server")
  (let [server (rj/run-jetty ring-handler {:port 5000 :join? false})]
    (println "web server listening on port 5000")
    server))

(defn -main [& args]
  (start))
