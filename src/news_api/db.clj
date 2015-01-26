(ns news-api.db)

(def users (ref []))

(defn save-new-user
  "Uses STM to save a new user and return the ID of the new user without
   worrying about race conditions."
  [user]
  (dosync
    (alter users conj user)
    (count @users)))
