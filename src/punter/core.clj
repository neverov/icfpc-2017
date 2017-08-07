(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [punter.strategies.random :as strategy]
            [punter.api :as api]
            [punter.tcp :as tcp]
            [punter.util :refer [log]]
            [punter.utils :as utils]
            [punter.strategies.basic :as basic]
            [punter.strategies.core   :as core]))

(def host "punter.inf.ed.ac.uk")
(def username "Lambda Riot")

(declare play-online)
(declare play-offline)
(declare play)
(declare run-offline)

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
        ; move (atom (api/recv-msg conn))
        strategy (-> (basic/->make) (core/init initial-state))
        _ (api/ready conn {:punter punter :state strategy})
        move (atom {})]
    (loop [strategy strategy]
      (reset! move (api/recv-msg conn))
      (log @move)
      (when-not (stop? @move)
        (let [{:keys [state claim]} (basic/move* strategy (-> @move :move :moves))]
          (api/move conn {:claim claim})
          (recur state))))))
