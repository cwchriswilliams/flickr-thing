(ns flickr-fetcher.controllers.fetcher
  (:require
   [flickr-fetcher.lib.fetcher :as fetchr]
   [cheshire.core :as json]
   [ring.util.response :as resp]
   [clojure.string :as string]))

; This is set based on the maximum number returned
(def max-take-value "The maximum allowable take value" 20)

; This is an arbitrary safe height/width
(def max-resize-heightwidth 65535)

(defn -append-error-to-state
  "Appends the new error to the error state map
  Arguments:
    - An error state map with a :current-errors key
    - A new error string
  Returns:
    - The error state map with the new error appended"
  [error-state new-error]
  (merge-with conj error-state {:current-errors new-error}))

(defn get-take-errors
  "Gets errors associated with the take parameter and defaults the value if not provided
  Arguments:
    - An error state map containing the keys :current-errors and :req-params
  Returns:
    - A new error-state with the errors appended and the take value set to default if not provided"
  [{:keys [current-errors req-params] :as error-state}]
  (let [with-default (into req-params {:take (get req-params :take max-take-value)})
        take-value (:take with-default)]
    (cond
      ((complement pos-int?) take-value) (-append-error-to-state error-state "take must be a positive integer")
      (> take-value 20) (-append-error-to-state error-state "take must be between 0 and 21")
      :else {:current-errors current-errors :req-params with-default})))

(defn get-resize-spec-errors
  "Gets errors associated with the resize-spec parameter
  Arguments:
    - An error state map containing the keys :current-errors and :req-params
  Returns:
    - A new error-state with the errors appended"
  [{:keys [req-params] :as error-state}]
  (let [resize-spec (:resize-spec req-params)]
    (if resize-spec
      (let [{:keys [width height maintain-ratio?]} resize-spec]
        (cond
          (nil? (or width height)) (-append-error-to-state error-state "At least one of height or width must be provided in resize-spec")
          (and (some? width) (or ((complement pos-int?) width) (> width max-resize-heightwidth)))
          (-append-error-to-state error-state "width must be between 0-65536")
          (and (some? height) (or ((complement pos-int?) height) (> height max-resize-heightwidth)))
          (-append-error-to-state error-state "height must be between 0-65536")
          (and (some? maintain-ratio?) ((complement boolean?) maintain-ratio?) maintain-ratio?) (-append-error-to-state error-state "maintain-ratio? must be true or false")
          :else
          error-state))
      error-state)))


(defn get-parameter-errors
  "Gets the errors associated with the current parameters
  Arguments:
    - A map of parameters
  Returns:
    - An error-state map with the keys :current-errors and :req-params"
  [req-params]
  (-> {:current-errors [] :req-params req-params}
      get-take-errors
      get-resize-spec-errors))

(defn fetchr-get-photos
  "Downloads photos from the flickr url
  Arguments:
    - A request object with an optional :take parameter
  Returns:
    - json collection of downloaded images or bad request"
  [req]
  (as-> req input
    (:body input)
    (let [{:keys [current-errors req-params]} (get-parameter-errors input)]
      (if (seq current-errors)
        (resp/bad-request (string/join "\n" current-errors))
        (as-> req-params in
          (map in [:take :resize-spec]) ; pull parameters into vector in order
          (filter (complement nil?) in)
          (apply fetchr/get-photos in)
          (json/generate-string in)
          (resp/response in)
          (resp/content-type in "application/json"))))))

