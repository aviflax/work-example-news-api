(ns news-api.core
  (:require [clojure.string :refer [blank?]]
            [resourceful :refer [resource]]
            [compojure [core :refer [GET POST routes]]
                       [route :refer [not-found]]]
            [ring.util.response :refer [get-header]]
            [ring.adapter.jetty :as rj]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def api-keys #{"13tm31n"})
(def users (ref []))

(defn save-new-user
  "Uses STM to save a new user and return the ID of the new user."
  [user]
  (dosync
    (alter users conj user)
    (count @users)))

(defn error-response [code message]
  {:status code
   :headers {"Content-Type" "text/plain;charset=UTF-8"}
   :body message})

(defn int-string?
    "Accepts a string, returns true if the string can be parsed as an integer, otherwise false."
    [s]
    (try
        (boolean (Integer/parseInt s))
        (catch NumberFormatException e false)))

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

        (some nil? [name address phone (:zip address)])
        (error-response 400 "The keys 'name', 'address', and 'phone' are required.")

        (some blank? [name phone (:zip address)])
        (error-response 400 "The keys 'name', 'address', and 'phone' are required.")

        (empty? address)
        (error-response 400 "The keys 'name', 'address', and 'phone' are required.")

        (or (not (int-string? (:zip address)))
            (not (= (.length (:zip address)) 5)))
        (error-response 400 "The key 'zip' must be a 5-digit number.")

        :default
        (let [new-user-id (save-new-user user)]
          {:status 201
           :headers {"Content-Type" "application/json"
                     "Location" (str "http://localhost:5000/users/" new-user-id)}
           :body user})))))

(def a-user
  (resource "a user"
    "/users/:id"
    (GET req "hello")))

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
              a-user)
      (wrap-json-body {:keywords? true :bigdecimals? true})
      wrap-json-response
      wrap-authentication))

(defn start []
  (println "starting web server")
  (let [server (rj/run-jetty ring-handler {:port 5000 :join? false})]
    (println "web server listening on port 5000")
    server))

(defn -main [& args]
  (start))
