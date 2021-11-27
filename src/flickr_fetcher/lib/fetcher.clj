(ns flickr-fetcher.lib.fetcher
  "Fetches atom data from flickr public feed and returns the image urls"
  (:require [clj-http.client :as client]
            [hickory.core :as hickory]
            [hickory.select :as selector]
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
   (build-download-path file-name (:download-path (config/get-config))))
  )

(defn string->hickory
  "Converts a string into hickory format data
  Arguments:
    - Input text which represents valid html
  Returns:
    - Input converted to hickory format"
  [input]
  (hickory/as-hickory (hickory/parse input)))

(defn public-photo-entries-selector
  "Extracts the `entry->jpeg` elements from the atom data of the input
  Arguments:
    - input in hickory format
  Returns:
    - A collection of entries extracted from hickory"
  [input]
  (selector/select
   (selector/child
    (selector/tag :entry)
    (selector/attr :type #(= "image/jpeg" %))) input))

(defn get-href-attribute-from-hickory-entry
  "Gets the href attribute from a hickory entry
  Arguments:
    - A hickory entry
  Returns:
    - A href string"
  [entry-hick]
  (get-in entry-hick [:attrs :href]))

(defn get-href-attribte-from-hickory-entries
  "Gets the href attribute from hickory entries
  Arguments:
    - A collection of hickory entries
  Returns:
    - A collection of href strings"
  [entries-hick]
  (map get-href-attribute-from-hickory-entry entries-hick))

(defn get-image-urls-from-public-photo-atom-data
  "Gets the image urls from the atom data provided
  Arguments:
    - The atom data as text
  Returns:
    - A collection of image urls"
  [atom-data]
  (-> atom-data
      string->hickory
      public-photo-entries-selector
      get-href-attribte-from-hickory-entries))

(defn download-image-from-url
  "Downloads an image from a url to the path defined in config or the provided destination
  Arguments:
    - url to download from
    - (opt) destination to download to"
  ([url]
   (let [file-name (get-file-name-from-url url)
         destination (build-download-path file-name)]
     (download-image-from-url url destination)))
  
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
    - A resize function to apply (takes a stream and returns an Image)"
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
     (download-and-resize-image-from-url url destination resize-fn))))

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
    height #(resizer/resize-to-height % height))
  )


(defn download-and-resize-images-from-urls
  "Downloads and resizes and image based on a resize-spec
  Arguments:
    - Collection of urls to image files
    - A resize spec defining how the resize should be performed
  Returns:
    - A list of urls the files were downloaded from"
  [urls resize-spec]
  (let [resize-fn (make-resize-fn resize-spec)]
    (pmap #(download-and-resize-image-from-url % resize-fn) urls))
  urls)

(defn download-images
  "Downloads the images from the urls to the destination specified in config
  Arguments:
    - urls to download from
  Returns:
    - The urls downloaded from"
  [urls]
  (pmap download-image-from-url urls)
  urls)

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
   get-image-urls-from-public-photo-atom-data
   ((partial take cnt))
   vec))

(defn get-photos
  "Downloads the images from the flickr url defined in the config
  Arguments:
    - Number of photos to retrieve
    - (opt) A resize spec defining how the resize should be performed
  Returns:
    - A collection of images downloaded"
  ([cnt]
   (-> cnt pull-photo-urls download-images))
  ([cnt resize-spec]
   (-> cnt pull-photo-urls (#(download-and-resize-images-from-urls % resize-spec)))))


(comment
  (def example-data (slurp "test/flickr_fetcher/data/example.xml"))
  (get-image-urls-from-public-photo-atom-data example-data)
  (def hicko (hickory/as-hickory (hickory/parse example-data)))
  (def selected-tags (selector/select (selector/child (selector/tag :entry) (selector/attr :type #(= "image/jpeg" %))) hicko))
  )
