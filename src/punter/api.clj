(ns punter.api
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]
            [punter.util :refer [log]]))

(defn- send-msg [ch payload]
  (let [body (generate-string payload)
        length (count body)
        msg (str length ":" body)]
    (tcp/write ch msg)))
    ;(log "sending message:" msg)
    ;(println msg)))

(defn init [ch name]
  (send-msg ch {:me name}))

(defn ready [ch punter]
  (send-msg ch {:ready punter}))

(defn move [ch punter source target]
  (send-msg ch {:claim {:punter punter :source source :target target}}))

(defn pass [ch punter]
  (send-msg ch {:pass {:punter punter}}))
