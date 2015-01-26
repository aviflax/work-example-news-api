(ns news-api.resources.users-collection
  (:require [news-api [db :refer [save-new-user users]]
                      [util :refer [error-response add-links-to-user]]]
            [resourceful :refer [resource]]
            [compojure.core :refer [POST]]
            [schema.core :as s]
            [ring.util.response :refer [charset content-type created get-header]]))

(def ^:private NonEmptyString (s/both s/Str (s/pred seq)))

(def ^:private check-user
  (s/checker
    {:name NonEmptyString
     :phone NonEmptyString
     :address {:zip #"\d{5}"
             s/Str s/Any}}))

(def users-collection
  (resource "collection of users"
    "/users"
    (POST
      {user :body :as req}
      (cond
        (nil? (get-header req "Content-Type"))
        (error-response 400 "The request must include the header Content-Type.")

        (not (.contains (get-header req "Content-Type") "application/json"))
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
