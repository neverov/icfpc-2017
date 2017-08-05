(ns punter.api
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]))

(defn- send-msg [ch payload]
  (let [msg (generate-string payload)
        length (count msg)]
    (tcp/write ch (str length ":" msg))))

(defn init [ch name]
  (let [payload {:me name}]
    (send-msg ch payload)))

(defn ready [ch punter]
  (let [payload {:ready punter}]
    (send-msg ch payload)))

(defn move [ch punter source target]
  (let [payload {:claim {:punter punter :source source :target target}}]
    (send-msg ch payload)))

(defn pass [ch punter]
  (let [payload {:pass {:punter punter}}]
    (send-msg ch payload)))
