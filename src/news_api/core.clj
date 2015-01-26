(ns news-api.core
  (:require [compojure [core :refer [routes]]
                       [route :refer [not-found]]]
            [news-api.util :refer [error-response]]
            [news-api.resources [a-user :refer [a-user]]
                                [user-news :refer [user-news]]
                                [users-collection :refer [users-collection]]]
            [ring.util.response :refer [get-header]]
            [ring.adapter.jetty :as rj]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def api-keys #{"13tm31n"})

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
