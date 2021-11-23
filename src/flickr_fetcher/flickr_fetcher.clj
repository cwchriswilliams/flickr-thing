(ns flickr-fetcher.flickr-fetcher
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [flickr-fetcher.server.management :as svr-mgmnt])
  (:gen-class))

(def cli-options
  "A definition of CLI options as defined in clojure.tools.cli"
  [["-p" "--port PORT" "Listen port" :default 80 :parse-fn #(Integer/parseInt %) :validate [#(< 0 % 0x10000) "Must be an integer between 0 and 65536"]]
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
    (cond
      (:help options) {:exit-message (get-usage-text summary)}
      errors {:exit-message (string/join \newline errors) :exit-status 1}
      :else {:options options})))


(defn -main
  "Evaluates the arguments and starts the server based on provided args

  Arguments:
    - args as strings as defined in documentation"
  [& args]
  (let [{:keys [exit-message exit-status options]} (process-cli-args args cli-options)]
    (if exit-message
      (do (println exit-message)
          (or exit-status 0))
      (svr-mgmnt/start-server (:port options)))))

(comment
  (svr-mgmnt/start-server 5556)
  (svr-mgmnt/restart-server))
