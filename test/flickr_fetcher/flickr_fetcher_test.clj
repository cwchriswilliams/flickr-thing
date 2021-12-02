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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Integration tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest ^:integration parse-args-and-start-service-with-fn-map-test
  (testing "If exception is thrown returns linked exit-code"
    (is (= -1 (sut/parse-args-and-start-service-with-fn-map {:start-service-fn (fn [_] (throw (ex-info "Test" {:type :unexpected}))) :log-fn (fn [_] :no-action) :print-fn (fn [_] :no-action)} "")))
    (is (= 1 (sut/parse-args-and-start-service-with-fn-map {:start-service-fn (fn [_] (throw (ex-info "Test" {:type :failed-to-parse-args}))) :log-fn (fn [_] :no-action) :print-fn (fn [_] :no-action)} "")))
    (is (= 2 (sut/parse-args-and-start-service-with-fn-map {:start-service-fn (fn [_] (throw (ex-info "Test" {:type :failed-to-load-config}))) :log-fn (fn [_] :no-action) :print-fn (fn [_] :no-action)} ""))))
  (testing "If exception is thrown with no ex-info returns -1 status code"
    (is (= -1 (sut/parse-args-and-start-service-with-fn-map {:start-service-fn (fn [_] (throw Exception)) :log-fn (fn [_] :no-action) :print-fn (fn [_] :no-action)} ""))))
  (testing "If help parameter is provided, returns ok status code"
    (is (= 0 (sut/parse-args-and-start-service-with-fn-map {:start-service-fn (fn [_] :noaction) :log-fn (fn [_] :no-action) :print-fn (fn [_] :no-action)} ["-h"]))))
  (testing "If no exceptions are thrown and help parameter not provided, returns ok"
    (is (= 0 (sut/parse-args-and-start-service-with-fn-map {:start-service-fn (fn [_] :noaction) :log-fn (fn [_] :no-action) :print-fn (fn [_] :no-action)} [""])))))

(deftest ^:integration process-cli-args-integration-test
  (testing "If port or appsettings are not provided, defaults port and appsettings path"
    (is (= {:port sut/default-port :config sut/default-app-settings-path} (:options (sut/process-cli-args "" sut/cli-options))))
    (is (= {:port 5050 :config sut/default-app-settings-path} (:options (sut/process-cli-args ["--port" "5050"] sut/cli-options)))))
  (is (= {:port sut/default-port :config "config-path"} (:options (sut/process-cli-args ["--config" "config-path"] sut/cli-options)))))
(testing "If port is provided sets port"
  (is (= {:port 5050 :config sut/default-app-settings-path} (:options (sut/process-cli-args ["--port" "5050"] sut/cli-options))))
  (is (= {:port 5050 :config sut/default-app-settings-path} (:options (sut/process-cli-args ["-p" "5050"] sut/cli-options))))
  (testing "If config is provided sets config"
    (is (= {:port sut/default-port :config "config-path"} (:options (sut/process-cli-args ["--config" "config-path"] sut/cli-options))))
    (is (= {:port sut/default-port :config "config-path"} (:options (sut/process-cli-args ["-c" "config-path"] sut/cli-options)))))
  (testing "If help is provided sets config"
    (is (= {:port sut/default-port :config sut/default-app-settings-path :help true} (:options (sut/process-cli-args ["--help"] sut/cli-options))))
    (is (= {:port sut/default-port :config sut/default-app-settings-path :help true} (:options (sut/process-cli-args ["-h"] sut/cli-options)))))
  (testing "If port is provided without value throws exception"
    (is (thrown? Exception (sut/process-cli-args ["--port"] sut/cli-options))))
  (testing "If config is provided without value throws exception"
    (is (thrown? Exception (sut/process-cli-args ["--config"] sut/cli-options)))))
