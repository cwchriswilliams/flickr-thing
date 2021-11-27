(ns flickr-fetcher.flickr-fetcher
  "Starts a Flickr Fetcher server"
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [flickr-fetcher.server.management :as svr-mgmnt]
            [flickr-fetcher.lib.config :as config]
            [flickr-fetcher.interop.log :as log])
  (:gen-class))

(def exit-codes {:unexpected-error -1 :ok 0 :failed-to-parse-args 1 :failed-to-load-config 2})

(def cli-options
  "A definition of CLI options as defined in clojure.tools.cli"
  [["-p" "--port PORT" "Listen port" :default 80 :parse-fn #(Integer/parseInt %) :validate [#(< 0 % 0x10000) "Must be an integer between 0 and 65536"]]
   ["-c" "--config PATH" "Path to the configuration file" :default "./appsettings.json"]
   ["-h" "--help" "Display usage information"]])

(defn get-usage-text
  "Gets a text string explaining how to use the application
  Arguments:
    - The summary text of usage arguments
  Returns:
    - A string detailing how to use the application"
  [cli-options-summary]
  (apply str "Flickr Fetcher starts a service on the provided port which gets and transforms images from the Flickr feed https://www.flickr.com/services/feeds/docs/photos_public/
  
  Usage:
"
         cli-options-summary))

(defn process-cli-args
  "Processes the provided cli args against the provided cli-options
  Arguments:
    - Command line args to parse
    - cli-options as defined in clojure.tools.cli
  Returns:
    - Map containing :exit-message (opt) - string to display to the user on the cli
                     :exit-status (opt) - error exit status if an error occurred
                     :options (opt) - The options provided on the command line"
  [args cli-options]
  (let [{:keys [errors options summary]} (cli/parse-opts args cli-options)]
    (if (some? errors)
      (throw (ex-info (str "Failed to parse CLI args:\n" (string/join \newline errors)) {:type :failed-to-parse-args}))
      {:options options :summary summary})))


(defn start-service
  [options]
  (config/load-config (:config options))
  (svr-mgmnt/start-server (:port options)))

(defn get-exit-code-from-exception
  [ex]
  (let [data (or (ex-data ex) {})
           ex-type (get data :type :unexpected-error)]
    (get exit-codes ex-type)))

(defn -main
  "Evaluates the arguments and configuration file and starts the server based on provided args

  Arguments:
    - args as strings as defined in documentation"
  [& args]
  (try
    (let [{:keys [summary options]} (process-cli-args args cli-options)]
      (if (:help options)
        (println (get-usage-text summary))
        (start-service options))
      (:ok exit-codes))
    (catch Exception ex
      (log/error (str ex))
      (println (ex-message ex))
      (get-exit-code-from-exception ex))))

(comment
  (svr-mgmnt/start-server 5556)
  (svr-mgmnt/restart-server))