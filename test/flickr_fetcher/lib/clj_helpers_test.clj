(ns flickr-fetcher.lib.clj-helpers-test
  (:require [clojure.test :refer [testing is deftest]]
            [flickr-fetcher.lib.clj-helpers :as sut]))

(deftest maybe-continue-test
  (testing "Returns result of applying function to input if predicate succeeds"
    (is (= 8 (sut/maybe-continue int? 7 inc)))
    (is (= 6 (sut/maybe-continue int? 7 dec)))
)
(testing "Returns input if predicate fails"
  (is (= 7 (sut/maybe-continue string? 7 inc)))
  (is (= 7 (sut/maybe-continue string? 7 dec)))
))