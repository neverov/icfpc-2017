(ns punter.api
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]))

(defn send-msg [conn payload]
  (let [body (generate-string payload)
        length (inc (count body))
        msg (str length ":" body "\n")]
    (tcp/write conn msg)))

(defn recv-msg [conn]
  (let [resp (tcp/readln conn)
        payload (second (clojure.string/split resp #":" 2))
        msg (parse-string payload true)]
    msg))

(defn init [conn name]
  (send-msg conn {:me name}))

(defn recv-you [conn]
  (recv-msg conn))

(defn recv-state [conn]
  (recv-msg conn))

(defn ready [conn punter]
  (send-msg conn {:ready punter}))

(defn move [conn move]
  (send-msg conn move))

(defn pass [conn punter]
  (send-msg conn {:pass {:punter punter}}))
