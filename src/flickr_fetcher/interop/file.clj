(ns flickr-fetcher.interop.file
  "Helpers for wrapping interop file code with clojure"
  (:require [clojure.java.io :as io]))

(defn does-file-exist
  "Checks if the provided file exists
  Arguments:
    - The file path to test
  Returns:
    - File if true otherwise false"
  [file-path]
  (let [file (io/as-file file-path)]
    (if (.exists file)
       file
       false))
  )