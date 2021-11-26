(ns flickr-fetcher.interop.log
  "Wrapper for the logger library to abstract interop."
  (:import [org.slf4j LoggerFactory]))

(defn set-log-config-path
  "Sets the log configuration path.
  If not called, `resources/logback.xml` will be used
  Arguments:
    - path to the logback configuration xml"
  [path]
  (System/setProperty "logback.configurationFile" path)
)

(set-log-config-path "resources/logback.xml")

(def -logger ^ch.qos.logback.classic.Logger (LoggerFactory/getLogger "flickr-fetcher"))

(defn info [msg] (.info -logger msg))