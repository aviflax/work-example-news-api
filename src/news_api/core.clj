(ns news-api.core
  (:require [resourceful :refer [resource]]
            [compojure [core :refer [POST routes]]
                       [route :refer [not-found]]]
            [ring.util.response :refer [get-header]]
            [ring.adapter.jetty :as rj]
            [ring.middleware.json :refer [wrap-json-params]]))


(def api-keys #{"13tm31n"})
(def users (atom []))


(defn error-response [code message]
  {:status code
   :headers {"Content-Type" "text/plain;charset=UTF-8"}
   :body message})


(def users-collection-resource
  (resource "collection of users"
    "/users"
    (POST
      {headers :headers
       {:keys [name address phone]} :params
       :as req}
      (cond
        (nil? (get-header req "Content-Type"))
        (error-response 400 "The request must include the header Content-Type.")

        (not (.startsWith (get-header req "Content-Type") "application/json"))
        (error-response 415 "The request representation must be of type application/json.")

        :default
        "yay!"))))


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
  (-> (routes users-collection-resource)
      wrap-json-params
      wrap-authentication))


(defn start []
  (println "starting web server")
  (let [server (rj/run-jetty ring-handler {:port 5000 :join? false})]
    (println "web server listening on port 5000")
    server))


(defn -main [& args]
  (start))
