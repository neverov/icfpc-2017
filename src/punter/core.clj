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

(declare play-online)
(declare play-offline)
(declare play)

(defn -main [& args]
  (log "starting Lambda Riot punter")
  (play-offline))

(defn stop? [msg]
  (contains? msg :stop))

(defn handshake [conn]
  (let [_ (api/init conn username)
        you (api/recv-you conn)
        _ (log "received you:" you)
        msg (api/recv-msg conn)
        _ (log "received message:" msg)]
    msg))

(defn play-offline []
  (log "connecting offline")
  (let [conn (tcp/connect *in* *out*)
        msg (handshake conn)
        punter (:punter msg)
        move (:move msg)
        stop (:stop msg)
        state (:state msg)]
    (cond
      punter
      (api/send-msg conn {:ready punter
                          :state (utils/->game-state msg)})
      move
      (api/send-msg conn (strategy/move state))
      stop
      (do
        (log "received stop")
        (log "player" (:punter state) ", game finished, scores:" (:scores stop)))
      :else (log "unrecognized server message:" msg))))

(defn play-online [port]
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
