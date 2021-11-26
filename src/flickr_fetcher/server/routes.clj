(ns flickr-fetcher.server.routes
  "Defines the routes for the Flickr Fetcher"
  (:require [compojure.core :as cjre]
            [compojure.route :as route]
            [flickr-fetcher.lib.fetcher :as fetchr]
            [cheshire.core :as json]))

(cjre/defroutes flickr-fetcher
  (cjre/POST "/photos" [] #(json/generate-string (fetchr/get-photos %)))
  (route/not-found "Error. Page not found"))
