(ns flickr-fetcher.controllers.fetcher-test
  (:require [clojure.test :refer [deftest testing is]]
            [flickr-fetcher.controllers.fetcher :as sut]))

(deftest get-parameter-errors-test
  (testing "Sets take to default value if not provided"
    (is (= sut/max-take-value (-> (sut/get-parameter-errors {}) :req-params :take))))
  (testing "Missing parameters is not an error"
    (is (empty? (:current-errors (sut/get-parameter-errors {})))))
  (testing "Valid parameters is not an error"
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 1}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 5}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 20}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:resize-params {:height 5}}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 5 :resize-params {:height 1}}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 5 :resize-params {:height sut/max-resize-heightwidth :width 1}}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 5 :resize-params {:width sut/max-resize-heightwidth}}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 5 :resize-params {:width sut/max-resize-heightwidth :maintain-ratio? true}}))))
    (is (empty? (:current-errors (sut/get-parameter-errors {:take 5 :resize-params {:width 1 :maintain-ratio? false}})))))
  (testing "Returns error for invalid take values"
    (is (seq (:current-errors (sut/get-parameter-errors {:take "test"}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:take -1}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:take 0}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:take (inc sut/max-take-value)})))))
  (testing "Returns error for empty resize-spec"
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {}})))))
  (testing "returns error for invalid resize-spec"
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:height -1}}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:height 0}}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:height (inc sut/max-resize-heightwidth)}}))))
    
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:width -1}}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:width 0}}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:width (inc sut/max-resize-heightwidth)}}))))

    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:maintain-ratio? false}}))))
    (is (seq (:current-errors (sut/get-parameter-errors {:resize-spec {:height 5 :maintain-ratio? "test"}}))))))

(defn build-invalid-response [error-msg] {:body error-msg :headers {} :status 400})
(defn build-valid-response [in-map] {:body in-map :headers {"Content-Type" "application/json"} :status 200})

(deftest fetchr-get-photos-with-get-photos-fn-test
  (testing "Returns bad request if parameters do not match spec"
    (is (= (build-invalid-response sut/take-exceded-limit-error-string) (sut/fetchr-get-photos-with-get-photos-fn {:body {:take 200}} (fn [_] [{:url "sample-url-1.jpg"}]))))
    (is (= (build-invalid-response sut/resize-spec-height-and-width-missing-error-string) (sut/fetchr-get-photos-with-get-photos-fn {:body {:resize-spec {}}} (fn [_ _] [{:url "sample-url-1.jpg"}])))))
  (testing "Returns OK request with return of get-photos-fn wrapped as json on valid parameters"
    (is (= (build-valid-response "[{\"url\":\"sample-url-1.jpg\"}]") (sut/fetchr-get-photos-with-get-photos-fn {:body {:take 20}} (fn [_] [{:url "sample-url-1.jpg"}]))))
    (is (= (build-valid-response "[{\"url\":\"sample-url-2.jpg\"}]") (sut/fetchr-get-photos-with-get-photos-fn {:body {:take 20}} (fn [_] [{:url "sample-url-2.jpg"}]))))
    (is (= (build-valid-response "[{\"url\":\"sample-url-2.jpg\"}]") (sut/fetchr-get-photos-with-get-photos-fn {:body {:resize-spec {:height 50}}} (fn [_ _] [{:url "sample-url-2.jpg"}]))))))