(ns news-api.util)

(defn error-response [code message]
  {:status code
   :headers {"Content-Type" "text/plain;charset=UTF-8"}
   :body message})

(defn add-links-to-user
  "Given a user map and its ID, add a :links key to the map and return it"
  [user id]
  (assoc user :links {:news (str "http://localhost:5000/users/" id "/news")}))
