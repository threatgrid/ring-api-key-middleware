(ns ring-api-key-middleware.core
  (:require [clojure.string :as s]))

(defn get-api-key
  "Given a ring request extract an api-key if it exists"
  [request]
  (when-let [auth-header (get-in request [:headers "authorization"])]
    (when-let [token (s/starts-with? auth-header "apiKey ")]
      (s/replace-first auth-header #"^apiKey " ""))))

(defn unauthorized
  "401 Unauthorized (ClientError)
  Authentication is possible but has failed or not yet been provided"
  [body]
  {:status 401
   :headers {}
   :body body})

(defn wrap-api-key-fn
  "I check "
  [handler get-infos]
  (fn [request]
    (if-let [api-key (get-api-key request)]
      (if-let [infos (get-infos api-key)]
        (handler (assoc request :api-key-infos infos))
        (unauthorized "wrong access key"))
      (handler request))))
