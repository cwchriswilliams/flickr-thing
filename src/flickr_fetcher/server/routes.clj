(ns flickr-fetcher.server.routes
  "Defines the routes for the Flickr Fetcher"
  (:require [compojure.core :as cjre]
            [compojure.route :as route]
            [flickr-fetcher.controllers.fetcher :as c-fetch]))


(cjre/defroutes flickr-fetcher
  (cjre/POST "/photos" req c-fetch/fetchr-get-photos)
  (route/not-found "Error. Page not found"))
