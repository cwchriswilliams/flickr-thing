(ns flickr-fetcher.lib.fetcher
  "Fetches atom data from flickr public feed and returns the image urls"
  (:require [clj-http.client :as client]
            [hickory.core :as hickory]
            [hickory.select :as selector]
            [flickr-fetcher.lib.config :as config]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [flickr-fetcher.interop.log :as log]))

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
  ([url] (let [file-name (get-file-name-from-url url)
               destination (build-download-path file-name)]
           (download-image-from-url url destination)))
  
  ([url destination]
   (log/info (str "Writing out image to " destination))
   (with-open [out (io/output-stream destination)
               in (io/input-stream url)]
     (io/copy in out))))

(defn download-images
  "Downloads the images from the urls to the destination specified in config
  Arguments:
    - urls to download from
  Returns:
    - The urls downloaded from"
  [urls]
  (pmap download-image-from-url urls)
  urls)

(defn get-photos
  "Downloads the images from the flickr url defined in the config
  Returns:
    - A collection of images downloaded"
  [_]
  (->
   (pull-public-photo-data)
   :body
   get-image-urls-from-public-photo-atom-data
   download-images
   ))


(comment

  (def example-data (slurp "test/flickr-fetcher/data/example.xml"))
  (get-image-urls-from-public-photo-atom-data example-data)
  (def hicko (hickory/as-hickory (hickory/parse example-data)))
  (def selected-tags (selector/select (selector/child (selector/tag :entry) (selector/attr :type #(= "image/jpeg" %))) hicko))

  )
