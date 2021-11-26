(ns flickr-fetcher.lib.config-test
  (:require [clojure.test :refer [testing is deftest]]
            [flickr-fetcher.lib.config :as sut]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def existing-file-path "test/flickr_fetcher/data/example.xml")
(def not-existing-file-path "test/flickr_fetcher/data/not-existing-file.xml")

(deftest validate-config-file-path-test
  (testing "Returns the file when file exists"
    (is (= (io/file existing-file-path) (sut/validate-config-file-path existing-file-path))))
  (testing "Returns error message when file does not exist"
    (is (= {:error-msg "Config file test/flickr_fetcher/data/not-existing-file.xml not found."} (sut/validate-config-file-path not-existing-file-path)))))


(deftest parse-config-test
  (testing "Returns map when valid json"
    (is (= {:field1 7 :field2 "value"} (sut/parse-config "{\"field1\": 7, \"field2\": \"value\"}")))
    (is (= {:field-1 "7" :field-2 33.0} (sut/parse-config "{\"field-1\": \"7\", \"field-2\": 33.0}"))))
  (testing "Returns error message when invalid json"
    (is (string/starts-with? (:error-msg (sut/parse-config "abc")) "Config file malformed "))))

(deftest coerce-config-test
  (testing "Returns config when matches spec"
    (is (= {:flickr-path "input-path" :download-path "output-path"} (sut/corece-config {:flickr-path "input-path" :download-path "output-path"})))
    (is (= {:flickr-path "input-path2" :download-path "output-path2" :optional-field "optional-value"} (sut/corece-config {:flickr-path "input-path2" :download-path "output-path2" :optional-field "optional-value"})))
    )
    (testing "Returns error string when does not match spec"
      (is (some? (:error-msg (sut/corece-config {}))))
      (is (some? (:error-msg (sut/corece-config {:flickr-path "input-path"}))))
      (is (some? (:error-msg (sut/corece-config {:flickr-path "input-path" :download-path 7}))))
      (is (some? (:error-msg (sut/corece-config {:flickr-path 7 :download-path "output-path"}))))
))

