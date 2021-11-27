(ns flickr-fetcher.server.routes
  "Defines the routes for the Flickr Fetcher"
  (:require [compojure.core :as cjre]
            [compojure.route :as route]
            [flickr-fetcher.lib.fetcher :as fetchr]
            [cheshire.core :as json]
            [clojure.spec.alpha :as spec]
            [ring.util.response :as resp]))

(def max-take-value 20)

(spec/def ::take (spec/and int? pos-int? #(<= % max-take-value)))


(defn is-take-param-valid?
  "Returns if the take-param of a req is valid (defaulting if not provided)
  Arguments:
    - A map representing the req-args
  Returns:
    - A map representing the req-args with take set to default if not provided"
  [req-params]
  (let [with-default (into req-params {:take (get req-params :take max-take-value)})]
    (if (spec/valid? ::take (:take with-default))
      with-default
      false)))

(defn fetchr-get-photos
  "Downloads photos from the flickr url
  Arguments:
    - A request object with an optional :take parameter
  Returns:
    - json collection of downloaded images or bad request"
  [req]
  (as-> req input
    (:body input)
    (if-let [param-map (is-take-param-valid? input)]
      (as-> param-map in
        (map in [:take]) ; pull parameters into vector in order
        (apply fetchr/get-photos in)
        (json/generate-string in))
      (resp/bad-request (str "Invalid take parameter")))))

(cjre/defroutes flickr-fetcher
  (cjre/POST "/photos" req fetchr-get-photos)
  (route/not-found "Error. Page not found"))
