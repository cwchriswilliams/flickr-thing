(ns flickr-fetcher.lib.config
  "Contains functions for loading configuration from a json file and validating. Config can be retrieved with the (get-config) function once loaded."
  (:require [flickr-fetcher.interop.file :as file-io]
            [cheshire.core :as json]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [flickr-fetcher.interop.log :as log]))

; Config file path is saved when set to allow reloading config
(defonce -config-file-path (atom nil))

; The config hash-map
(defonce -config (atom {}))

(defn get-config
  "Returns the config hash-map"
  [] @-config)

;;;;;;;;;;;;;;;;;;;;;;
; Config specs
;;;;;;;;;;;;;;;;;;;;;;
(spec/def ::download-path string?)
(spec/def ::flickr-path string?)
(spec/def ::config (spec/keys :req-un [::flickr-path ::download-path]))

;;;;;;;;;;;;;;;;;;;;;;
; End Config Specs
;;;;;;;;;;;;;;;;;;;;;;

(defn validate-config-file-path
  "Validates that config file path points to a file
  Arguments:
    - Path to existing config path
  Returns:
    - Java File to file if file exists
  Throws:
    - ex-info with :type :file-not-found if file does not exist"
  [file-path]
  (if-let [file (file-io/does-file-exist file-path)]
    file
    (throw (ex-info (str "Config file " file-path " not found.") {:type :file-not-found})))
  )

(defn parse-config
  "Validates that config file is valid json and parses
  Arguments:
    - text of the config file
  Returns:
    - hash-map of config data if valid
  Throws:
    - ex-info with :type :invalid-json if json not valid"
    [config-text]
  (try (json/parse-string config-text true)
       (catch Exception ex
         (throw (ex-info (ex-message ex) {:type :invalid-json} ex)))))

(defn read-config
  "Reads the config file and parses it
  Arguments:
    - Path to config file
  Returns:
    - Hash map of config file if valid otherwise map with :error-msg"
  [file]
  (-> file
      slurp
      parse-config)
)

(defn corece-config
  "Validates the config file against the spec
  Arguments:
    - config hash-map to validate
  Returns:
    - config hash-map if valid
  Throws:
    - ex-info with :type :invalid-config if does not match spec"
  [config]
  (let [result (spec/conform ::config config)]
    (if (spec/invalid? result)
      (throw (ex-info "Config does not match specification."
                       {:type :invalid-config
                        :spec-failure (spec/explain-data ::config config)
                        :spec-failure-msg (expound/expound-str ::config config)}))
      config)))

(defn store-config
  "Stores the config in an atom and logs
  Arguments:
    - config hash-map"
  [in-config]
  (reset! -config in-config)
  (log/info (str "Configuration Stored from " @-config-file-path)))


(defn reload-config
  "Loads or reloads the config from the file path in the -config-file-path atom and stores in the -config atom
  Throws:
    - ex-info with :type :failed-to-load-config if fails to load config file"
  []
    (try (->
     @-config-file-path
     validate-config-file-path
     read-config
     corece-config
     store-config)
         (catch Exception ex
           (throw (ex-info (ex-message ex) {:type :failed-to-load-config} ex)))))

(defn load-config
  "Loads the config into the -config atom from the provided file path or ./appsettings.json by default
  Throws:
    - ex-info with :type :failed-to-load-config if fails to load config file"
  ([config-file-path]
   (reset! -config-file-path config-file-path)
   (reload-config))
  ([] (load-config "./appsettings.json")))


(comment
  (load-config "./resources/appsettings.json")
  )