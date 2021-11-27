(ns flickr-fetcher.lib.config-test
  (:require [clojure.test :refer [testing is deftest]]
            [flickr-fetcher.lib.config :as sut]
            [clojure.java.io :as io]))

(def existing-file-path "test/flickr_fetcher/data/example.xml")
(def not-existing-file-path "test/flickr_fetcher/data/not-existing-file.xml")

(defn get-ex-data
  [f]
  (try (f)
       :no-exception-thrown
       (catch Exception ex 
         (:type (ex-data ex)))))

(deftest validate-config-file-path-test
  (testing "Returns the file when file exists"
    (is (= (io/file existing-file-path) (sut/validate-config-file-path existing-file-path))))
  (testing "Throws exception when file does not exist"
    (is (thrown? Exception (sut/validate-config-file-path not-existing-file-path)))
    (is (= :file-not-found (get-ex-data #(sut/validate-config-file-path not-existing-file-path))))))


(deftest parse-config-test
  (testing "Returns map when valid json"
    (is (= {:field1 7 :field2 "value"} (sut/parse-config "{\"field1\": 7, \"field2\": \"value\"}")))
    (is (= {:field-1 "7" :field-2 33.0} (sut/parse-config "{\"field-1\": \"7\", \"field-2\": 33.0}"))))
  (testing "Throws exception when invalid json"
    (is (thrown? Exception (sut/parse-config "abc")))
    (is (= :invalid-json (get-ex-data #(sut/parse-config "abc"))))
    ))

(deftest coerce-config-test
  (testing "Returns config when matches spec"
    (is (= {:flickr-path "input-path" :download-path "output-path"} (sut/corece-config {:flickr-path "input-path" :download-path "output-path"})))
    (is (= {:flickr-path "input-path2" :download-path "output-path2" :optional-field "optional-value"} (sut/corece-config {:flickr-path "input-path2" :download-path "output-path2" :optional-field "optional-value"})))
    )
    (testing "Throws exception when does not match spec"
      (is (thrown? Exception (:error-msg (sut/corece-config {}))))
      (is (= :invalid-config (get-ex-data #(sut/corece-config {}))))
      (is (= :invalid-config (get-ex-data #(sut/corece-config {:flickr-path "input-path"}))))
      (is (= :invalid-config (get-ex-data #(sut/corece-config {:flickr-path "input-path" :download-path 7}))))
      (is (= :invalid-config (get-ex-data #(sut/corece-config {:flickr-path 7 :download-path "output-path"}))))
))

