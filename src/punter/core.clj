(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [punter.strategies.random :as strategy]
            [punter.api :as api]
            [punter.tcp :as tcp]
            [punter.util :refer [log]]
            [punter.utils :as utils]
            [punter.strategies.basic :as basic]
            [punter.strategies.core :as core]))

(def host "punter.inf.ed.ac.uk")
;(def host "localhost")
(def username "Lambda Riot")

(defn -main [& args]
  (log "starting Lambda Riot punter")
  (log "args:" args)
  (log "punter initialized")
  (api/init nil username)
  (let [{:keys [in out]} (tcp/connect *in* *out*)
        you (.readLine in)
        _ (log you)
        state (.readLine in) 
        _ (log state)
        _ (api/ready nil (:punter state))]
    (while true 
      (let [move (.readLine in)]
        (log move)
        (api/pass nil 0)))))

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
  (let [conn (tcp/connect-online host port)
        initial-state (handshake conn)
        strategy (-> (basic/->make) (core/init initial-state))
        game-state (atom (utils/->game-state initial-state))
        punter (:punter @game-state)
        move (atom {})]
    (loop [strategy strategy]
      (reset! move (api/recv-msg conn))
      (when-not (stop? @move)
        (let [{:keys [state claim]} (basic/move* strategy (-> @move :move :moves))]
          (api/move conn {:claim claim})
          (recur state))))
    (log "received stop:" @move)
    (log "player" punter ", game finished, scores:" (:scores (:stop @move)))
    conn))
