(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [punter.strategies.random :as strategy]
            [punter.api :as api]
            [punter.tcp :as tcp]
            [punter.util :refer [log]]
            [punter.utils :as utils]))

(def host "punter.inf.ed.ac.uk")
(def username "Lambda Riot")

(defn play-offline [args]
  (log "starting Lambda Riot punter")
  (log "args:" args)
  (log "punter initialized")
  (let [conn (tcp/connect *in* *out*)
        _ (log "connection established, sending init")
        _ (api/init conn username)
        _ (log "init sent for:" username)
        you (api/recv-you conn)
        _ (log "received you")
        _ (log you)
        state (api/recv-state conn) 
        _ (log state)
        _ (api/ready conn (:punter state))]
    (while true 
      (let [move (api/recv-msg conn)]
        (log move)
        (api/pass nil 0)))))

(defn -main [& args]
  (play-offline args))

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
        game-state (atom (utils/->game-state initial-state))
        punter (:punter @game-state)
        move (atom (api/recv-msg conn))]
    (while (not (stop? @move))
      (log "received move:" @move)
      (swap! game-state utils/apply-move @move)
      (try
        (let [move (strategy/move @game-state)]
          (api/move conn move))
        (catch Exception e
          (log e)
          (api/pass conn punter)))
      (reset! move (api/recv-msg conn)))
    (log "received stop:" @move)
    (log "player" punter ", game finished, scores:" (:scores (:stop @move)))
    conn))
