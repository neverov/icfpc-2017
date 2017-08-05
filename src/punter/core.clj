(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [punter.strategies.random :as strategy]
            [punter.api :as api]
            [punter.tcp :as tcp]
            [punter.util :refer [log]]))

(def host "punter.inf.ed.ac.uk")
(def username "Lambda Riot")

(defn -main [& args]
  (log "starting Lambda Riot punter")
  (log "args:" args)
  (log "punter initialized")
  (api/init nil username)
  (doseq [ln (line-seq (java.io.BufferedReader. *in*))]
    (log ln)))

(defn stop? [msg]
  (contains? msg :stop))

(defn handshake [conn]
  (let [_ (api/init conn username)
        you (api/recv-you conn)
        _ (log "received you:" you)
        state (api/recv-state conn)
        _ (log "received initial game state:" state)
        _ (api/ready conn (:punter state))]
    state))

(defn play [port]
  (log "connecting:" (str host ":" port))  
  (let [conn (tcp/connect host port)
        state (handshake conn)
        punter (:punter state)
        move (atom (api/recv-msg conn))
        _ (api/pass conn (:punter state))
        _ (log @move)]
    (while (not (stop? @move))
      (reset! move (api/recv-msg conn))
      (log "received move:" @move) 
      (let [next-move (strategy/move state)]
        (api/send-msg conn next-move)))
    (log "player" punter ", game finished, scores:" (:scores (:stop @move)))
    conn))
