(ns flickr-fetcher.flickr-fetcher
  "Starts a Flickr Fetcher server"
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [flickr-fetcher.server.management :as svr-mgmnt]
            [flickr-fetcher.lib.config :as config]
            [flickr-fetcher.interop.log :as log])
  (:gen-class))

(def default-port 80)
(def default-app-settings-path "./appsettings.json")

(def exit-codes "Map of exception type to exit code"
  {:unexpected-error -1 :ok 0 :failed-to-parse-args 1 :failed-to-load-config 2})

(def cli-options
  "A definition of CLI options as defined in clojure.tools.cli"
  [["-p" "--port PORT" "Listen port" :default default-port :parse-fn #(Integer/parseInt %) :validate [#(< 0 % 0x10000) "Must be an integer between 0 and 65536"]]
   ["-c" "--config PATH" "Path to the configuration file" :default default-app-settings-path]
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

(defn process-cli-args-with-cli-options-and-parser
  "Processes the provided cli args against the provided cli-options
  Arguments:
    - Command line args to parse
    - cli-options as defined in clojure.tools.cli
  Returns:
    - Map containing :exit-message (opt) - string to display to the user on the cli
                     :exit-status (opt) - error exit status if an error occurred
                     :options (opt) - The options provided on the command line"
  [args cli-options parser]
  (let [{:keys [errors options summary]} (parser args cli-options)]
    (if (some? errors)
      (throw (ex-info (str "Failed to parse CLI args:\n" (string/join \newline errors)) {:type :failed-to-parse-args}))
      {:options options :summary summary})))

(defn process-cli-args
  "Processes the CLI options with the standard cli-parser
  Arguments:
    - A list of arguments as defined in the documentation
    - A cli-options as defined in clojure.tools.cli
  Returns:
    - Map containing :exit-message (opt) - string to display to the user on the cli
                     :exit-status (opt) - error exit status if an error occurred
                     :options (opt) - The options provided on the command line"
  [args cli-options]
  (process-cli-args-with-cli-options-and-parser args cli-options cli/parse-opts))

(defn start-service
  "Starts the service, loding the config and starting the server
  Arguments:
    - An options containing the keys :config and :port"
  [options]
  (config/load-config (:config options))
  (svr-mgmnt/start-server (:port options)))

(defn get-exit-code-from-exception
  "Gets the exit code for the exception provided
  Arguments:
    - An exception
  Returns:
    - An exit code, or -1 if the exception was unexpected"
  [ex]
  (let [data (or (ex-data ex) {})
        ex-type (get data :type :unexpected-error)]
    (get exit-codes ex-type -1)))

(defn parse-args-and-start-service-with-fn-map
  "Evaluates the arguments and configuration file and starts the server based on provided args

  Arguments:
    - A hash map with the keys :start-service-fn :print-fn :log-fn
    - args as strings as defined in documentation
  Returns:
    - An exit code to be returned to the user"
  [{:keys [start-service-fn print-fn log-fn]} args]
  (try
    (let [{:keys [summary options]} (process-cli-args args cli-options)]
      (if (:help options)
        (print-fn (get-usage-text summary))
        (start-service-fn options))
      (:ok exit-codes))
    (catch Exception ex
      (log-fn (str ex))
      (print-fn (ex-message ex))
      (get-exit-code-from-exception ex))))

(defn -main
  "Evaluates the arguments and configuration file and starts the server based on provided args

  Arguments:
    - args as strings as defined in documentation"
  [& args]
  (parse-args-and-start-service-with-fn-map {:start-service-fn start-service :print-fn println :log-fn log/error} args))

(comment
  (config/load-config "./resources/appsettings.json")
  (svr-mgmnt/start-server 5556)
  (svr-mgmnt/restart-server)
  
  (require '[clj-http.client :as client])
  (client/post "http://localhost:5556/photos" {:form-params {:take 2} :content-type :json})
  )