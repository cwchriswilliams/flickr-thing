(ns flickr-fetcher.interop.file-test
  (:require [clojure.test :refer [testing is deftest]]
            [flickr-fetcher.interop.file :as sut]
            [clojure.java.io :as io]))

(def existing-file-path "test/flickr_fetcher/data/example.xml")
(def not-existing-file-path "test/flickr_fetcher/data/not-existing-file.xml")

(deftest does-file-exist-test
  (testing "Returns file when the file exists"
    (is (= (io/file existing-file-path) (sut/does-file-exist existing-file-path))))
  (testing "Returns false when the file does not exist"
    (is (false? (sut/does-file-exist not-existing-file-path)))))