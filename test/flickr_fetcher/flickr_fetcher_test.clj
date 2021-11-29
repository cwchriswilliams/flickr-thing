(ns flickr-fetcher.flickr-fetcher-test
  (:require [clojure.test :refer [deftest testing is]]
            [flickr-fetcher.flickr-fetcher :as sut]))

(deftest process-cli-args-test
  (testing "Throws exception if parser returns errors"
    (is (thrown? Exception (sut/process-cli-args-with-cli-options-and-parser "" :cli-opts #(identity {:errors [] :options {} :summary ""}))))
    (is (thrown? Exception (sut/process-cli-args-with-cli-options-and-parser "" :cli-opts #(identity {:errors ["error-text"] :options {} :summary ""})))))
  (testing "Returns map of options and summary from parser if no errors"
    (is (=
         {:options {:field 7} :summary "summ-text"}
         (sut/process-cli-args-with-cli-options-and-parser "" :cli-opts (fn [_ _] (identity {:options {:field 7} :summary "summ-text"})))))
    (is (=
         {:options {:field2 2} :summary "summ-text2"}
         (sut/process-cli-args-with-cli-options-and-parser "" :cli-opts (fn [_ _] (identity {:options {:field2 2} :summary "summ-text2"})))))))

