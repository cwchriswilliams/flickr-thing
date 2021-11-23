(ns flickr-fetcher.interop.log
  (:import [org.slf4j LoggerFactory]))

(System/setProperty "logback.configurationFile" "resources/logback.xml")

(def -logger ^ch.qos.logback.classic.Logger (LoggerFactory/getLogger "flickr-fetcher"))

(defn info [msg] (.info -logger msg))