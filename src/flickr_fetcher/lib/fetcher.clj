(ns flickr-fetcher.lib.fetcher
  "Fetches json data from flickr public feed and returns the image urls"
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [flickr-fetcher.lib.config :as config]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [flickr-fetcher.interop.log :as log]
            [image-resizer.core :as resizer]
            [image-resizer.format :as img-format]))

(defn pull-public-photo-data
  "Retrieves the result of a get on the flickr-path from the config
  Returns:
    - The result of a get call to the flickr-path"
  []
  (client/get (:flickr-path (config/get-config))))

(defn get-file-name-from-url
  "Gets the file-name from a url
  Arguments:
    - Url to parse file-name from
  Returns:
    - string file-name"
  [url]
  (last (string/split url #"/")))

(defn build-download-path
  "Builds a path to download the file to from a file-name based on the value in config
  Arguments:
    - The file-name to save
    - (opt) folder path to save to
  Returns:
    - The path to download the file to"
  ([file-name download-path]
   (string/join [download-path file-name]))
  ([file-name]
   (build-download-path file-name (:download-path (config/get-config)))))

(defn extract-json-data-from-flickr-feed
  "Gets the json data from the flickr-feed text which for some reason is surrounded by jsonFlickrFeed(...)
  Arguments:
    - The json feed data
  Returns:
    - The json text data or an empty string if does not match expected format"
  [json-feed-text]
  (get (re-matches #"(?ms)^jsonFlickrFeed\((.*)\)$" json-feed-text) 1 ""))


(defn get-image-urls-from-public-photo-json-data
  "Gets the image urls from the json data provided
  Arguments:
    - The json data as text (surrounded with jsonFlickrFeed(...) for some reason)
  Returns:
    - A collection of image urls"
  [json-data]
  (-> json-data
      extract-json-data-from-flickr-feed
      (json/parse-string true)
      (get :items)
      ((partial map #(get-in % [:media :m])))))

(defn get-failed-to-download-error-msg
  [ex]
  (str "Failed to download image: " (ex-message ex)))

(defn download-image-from-url
  "Downloads an image from a url to the path defined in config or the provided destination
  Arguments:
    - url to download from
    - (opt) destination to download to
  Returns:
    - A hash-map with :url and (opt) :error"
  ([url]
   (let [file-name (get-file-name-from-url url)
         destination (build-download-path file-name)]
     (try
       (download-image-from-url url destination)
       {:url url}
       (catch Exception ex {:url url :error (get-failed-to-download-error-msg ex)}))))

  ([url destination]
   (log/info (str "Writing out image to " destination))
   (with-open [out (io/output-stream destination)
               in (io/input-stream url)]
     (io/copy in out))))

(defn download-and-resize-image-from-url
  "Downloads and resizes an image from a url to the path defined in config or the provided destination
  Arguments:
    - url to download from
    - (opt) destination to download to
    - A resize function to apply (takes a stream and returns an Image)
  Returns:
    - A hash-map with :url and (opt) :error"
  ([url destination resize-fn]
   (with-open [out (io/output-stream destination)
               in (io/input-stream url)]
     (-> in
         resize-fn
         (img-format/as-stream "jpg")
         (io/copy out))))
  ([url resize-fn]
   (let [file-name (get-file-name-from-url url)
         destination (build-download-path file-name)]
     (try
       (download-and-resize-image-from-url url destination resize-fn)
       {:url url}
       (catch Exception ex {:url url :error (get-failed-to-download-error-msg ex)})))))

(defn make-resize-fn
  "Builds a resize function based on a resize-spec
  Arguments:
    - A resize-spec with the keys (opt):width (opt):height (opt):maintain-ratio?. At least height or width must be provided
  Returns:
    - A resize function (takes a stream and returns an Image)"
  [{:keys [:width :height :maintain-ratio?]}]
  (cond
    (and width height (false? maintain-ratio?)) #(resizer/force-resize % width height)
    (and width height) #(resizer/resize % width height)
    width #(resizer/resize-to-width % width)
    height #(resizer/resize-to-height % height)))


(defn download-and-resize-images-from-urls
  "Downloads and resizes and image based on a resize-spec
  Arguments:
    - Collection of urls to image files
    - A resize spec defining how the resize should be performed
  Returns:
    - A list of urls hash-maps with :url and (opt) :error"
  [urls resize-spec]
  (let [resize-fn (make-resize-fn resize-spec)]
    (pmap #(download-and-resize-image-from-url % resize-fn) urls)))

(defn download-images
  "Downloads the images from the urls to the destination specified in config
  Arguments:
    - urls to download from
  Returns:
    - A list of urls hash-maps with :url and (opt) :error"
  [urls]
  (pmap download-image-from-url urls))

(defn pull-photo-urls
  "Gets the urls for the images from the flickr url defined in the config
  Arguments:
    - Number of photos to retrieve
  Returns:
    - A collection of images to download"
  [cnt]
  (->
   (pull-public-photo-data)
   :body
   get-image-urls-from-public-photo-json-data
   ((partial take cnt))
   vec))

(defn get-photos
  "Downloads the images from the flickr url defined in the config
  Arguments:
    - Number of photos to retrieve
    - (opt) A resize spec defining how the resize should be performed
  Returns:
    - A list of urls hash-maps with :url and (opt) :error"
  ([cnt]
   (-> cnt pull-photo-urls download-images))
  ([cnt resize-spec]
   (-> cnt pull-photo-urls (#(download-and-resize-images-from-urls % resize-spec)))))


(comment
  (def example-data (slurp "test/flickr_fetcher/data/example.json"))


  (extract-json-data-from-flickr-feed example-data)
  (def urls (get-image-urls-from-public-photo-json-data example-data))
  (def test-resize-spec {:height 50 :width 200 :maintain-ratio? false})
  (println urls)
  (download-and-resize-images-from-urls urls test-resize-spec)
  )
