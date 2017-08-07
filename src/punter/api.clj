(ns punter.api
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]
            [punter.util :refer [log]]))

(defn send-msg [conn payload]
  (let [body (generate-string payload)
        length (count body)
        msg (str length ":" body)]
    (tcp/write conn msg)))

(defn recv-msg [conn]
  (let [resp (tcp/read-msg conn)
        ;payload (second (clojure.string/split resp #":" 2))
        ; _ (clojure.pprint/pprint resp)
        msg (parse-string resp true)]
    msg))

(defn init [conn name]
  (send-msg conn {:me name}))

(defn recv-you [conn]
  (recv-msg conn))

(defn recv-state [conn]
  (recv-msg conn))

(defn ready [conn data]
  (send-msg conn data))

(defn move [conn move]
  (send-msg conn move))

(defn pass [conn punter]
  (send-msg conn {:pass {:punter punter}}))
