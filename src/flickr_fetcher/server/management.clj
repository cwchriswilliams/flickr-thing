(ns flickr-fetcher.server.management
  "Manages the flickr fetcher server.
  Note only one server can be active per process at one time.
  Key functions:
    - start-server
    - stop-server
    - restart-server"
  (:require [org.httpkit.server :as httpkit]
            [flickr-fetcher.server.routes :as routes]
            [ring.middleware.defaults :as ring]
            [ring.middleware.json :as ring-json]
            [flickr-fetcher.interop.log :as log]))

; The server instance for starting and stopping
(defonce app-server-instance (atom nil))

(defn start-server
  "Start the server instance on the provided port
  Arguments:
    - Port for the server to listen on"
  [port]
  (reset! app-server-instance
          (httpkit/run-server (ring-json/wrap-json-body (ring/wrap-defaults #'routes/flickr-fetcher ring/api-defaults)) {:port port}))
  (log/info (apply str "Server started on port " (str port))))

(defn stop-server
  "Gracefully stop the server after 100ms"
  []
  (when-not (nil? @app-server-instance)
    (@app-server-instance :timeout 100)
    (reset! app-server-instance nil)
    (log/info "Server stopped")))

(defn restart-server
  "Restarts the server on the same port"
  []
  (let [port (:local-port (meta @app-server-instance))]
    (stop-server)
    (start-server port)))
