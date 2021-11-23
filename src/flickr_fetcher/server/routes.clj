(ns flickr-fetcher.server.routes
  (:require [compojure.core :as cjre]
            [compojure.route :as route]))

(cjre/defroutes flickr-fetcher
  (cjre/GET "/" [] {:status 200 :body "Everything is awesome 4"})
  (route/not-found "Error. Page not found"))
