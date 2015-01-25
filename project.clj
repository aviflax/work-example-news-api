(defproject news-api "0.1.0-SNAPSHOT"
  :description "News API"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"] ; includes ring-core
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ring/ring-json "0.3.1" :exclusions [cheshire]] ; bring in newer Cheshire below
                 [resourceful "0.1.1" :exclusions [ring-core compojure]] ; Resourceful is out of date
                 [com.twinql.clojure/clj-conneg "1.1.0"]
                 [cheshire "5.4.0"]
                 [prismatic/schema "0.3.3"]]
  :main news-api.core
  :plugins [[lein-ring "0.9.1"]]
  :ring {:handler news-api.core/ring-handler})
