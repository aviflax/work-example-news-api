(ns news-api.resources.a-user
  (:require [news-api [db :refer [users]]
                      [util :refer [error-response add-links-to-user]]]
            [clojure [string :refer [blank?]]
                     [zip :as zip]]
            [resourceful :refer [resource]]
            [compojure.core :refer [GET]]
            [clojure.data.xml :refer [element] :as xml]
            [ring.util.response :refer [charset content-type created get-header]]))

(defn ^:private user-to-xml [user]
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
      {{:keys [id]} :params
       :as req}
      (let [user-id (Integer/parseInt id) ; TODO: return a 404 if this fails
            user-index (dec user-id) ; because the users vector is 0-indexed
            user (get @users user-index)
            user-with-links (add-links-to-user user user-id)
            accept (get-header req "Accept")]
        (cond
          (nil? user)
          (error-response 404 "No such resource.")

          (and (not (nil? accept))
               (not (blank? accept))
               (not (.contains accept "*/*"))
               (not (.contains accept "application/*"))
               (not (.contains accept "application/json"))
               (not (.contains accept "application/xml")))
          (error-response 406 "This resource supports only application/json or application/xml.")

          (.startsWith (get-header req "Accept") "application/xml")
          {:status 200
           :headers {"Content-Type" "application/xml;charset=UTF-8"}
           :body (xml/emit-str (user-to-xml user-with-links))}

          :default
          {:status 200
           :headers {"Content-Type" "application/json;charset=UTF-8"}
           :body user-with-links})))))
