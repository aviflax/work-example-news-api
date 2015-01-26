(ns news-api.resources.user-news
  (:require [news-api [db :refer [users]]
                      [util :refer [error-response add-links-to-user]]]
            [clojure.string :refer [blank?]]
            [clojure.core.cache :as cache]
            [resourceful :refer [resource]]
            [compojure.core :refer [GET]]
            [environ.core :refer [env]]
            [clj-http.client :as client]
            [ring.util.response :refer [charset content-type created get-header]]))

(def ^:private weather-cache (atom (cache/ttl-cache-factory {} :ttl 300000)))

(def ^:private news-cache (atom (cache/ttl-cache-factory {} :ttl 300000)))

(defn ^:private get-coords [zip]
  (let [uri (str "http://maps.googleapis.com/maps/api/geocode/json?address=" zip)
        response (client/get uri {:accept :json, :as :json-strict})]
    (get-in response [:body :results 0 :geometry :location])))

(def ^:private get-coords-m (memoize get-coords))

(defn ^:private get-forecast [zip]
  (let [{:keys [lat lng]} (get-coords-m zip)
        base-uri "https://api.forecast.io/forecast/"
        api-key (env :forecast-key)
        uri (str base-uri api-key "/" lat "," lng)
        response (client/get uri {:accept :json, :as :json-strict})]
    (:body response)))

(defn ^:private get-forecast-cached [zip]
  (if (cache/has? @weather-cache zip)
      (swap! weather-cache #(cache/hit % zip))
      (swap! weather-cache #(cache/miss % zip (get-forecast zip))))
  (cache/lookup @weather-cache zip))

(defn ^:private get-headlines [zip]
  ;; TODO: These are specifically relevant to the provided zipcode but so far I haven’t found an
  ;; API that provides such data
  (let [api-key (env :usatoday-articles-key)
        uri (str "http://api.usatoday.com/open/articles/topnews?encoding=json&api_key=" api-key)
        response (client/get uri {:accept :json, :as :json-strict})
        stories (get-in response [:body :stories])]
    (map :description stories)))

(defn ^:private get-headlines-cached [zip]
  (if (cache/has? @news-cache :headlines)
      (swap! news-cache #(cache/hit % :headlines))
      (swap! news-cache #(cache/miss % :headlines (get-headlines zip))))
  (cache/lookup @news-cache :headlines))

(def user-news
  (resource "the news for a user"
    "/users/:id/news"
    (GET
      {{:keys [id]} :params
       :as req}
      (let [user-id (Integer/parseInt id) ; TODO: return a 404 if this fails
            user-index (dec user-id) ; because the users vector is 0-indexed
            user (get @users user-index)
            zip (get-in user [:address :zip])
            accept (get-header req "Accept")]
        (cond
          (nil? user)
          (error-response 404 "No such resource.")

          (and (not (nil? accept))
               (not (blank? accept))
               (not (.contains accept "*/*"))
               (not (.contains accept "application/*"))
               (not (.contains accept "application/json")))
          (error-response 406 "This resource supports only application/json.")

          :default
          {:status 200
           :headers {"Content-Type" "application/json;charset=UTF-8"}
           :body {:weather {:forecast (get-forecast-cached zip)}
                  :news {:headlines (get-headlines-cached zip)}}})))))
