(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
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
(declare run-offline)

(defn -main [& args]
  (log "starting Lambda Riot punter")
  (run-offline))  

(defn stop? [msg]
  (contains? msg :stop))

(defn handshake [conn]
  (let [_ (log "sending init")
        _ (api/init conn  username)
        m (tcp/read-json conn)
        _ (log "message:" m)
        you (api/recv-you conn)
        _ (log "received you:" you)
        state (api/recv-state conn)
        _ (log "received initial game state:" state)
        _ (api/ready conn (:punter state))]
    state))

(defn play-offline []
  (log "connecting offline")
  (play (tcp/connect *in* *out*)))

(defn play-online [port]
  (log "connecting:" (str host ":" port))
  (play (tcp/connect-online host port)))

(defn play [conn]
  (let [initial-state (handshake conn)
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

(defn send-json
  [out data]
  (let [encoded (json/write-str data)
        l (count encoded)
        payload (format "%d:%s" (count encoded) encoded)]
    (log "sent: " payload)
    (.write out (.toCharArray payload))
    (.flush out)))

(defn read-json
  [in]
  ;; Throw away the length leader
  (while (not= \: (char (.read in))))
  (let [val (json/read in)]
    (log "read:" (pr-str val))
    val))

(defn handshake
  [name in out]
  (log "Starting handshake read")
  (send-json out {"me" name})
  (read-json in))

(defn run-offline []
  (let [name username
        in *in*
        out *out*]
    (log "Starting an offline game")
    (handshake name in out)
    (loop [msg (read-json in)]
      (log "Entering run loop")
      (log "Received message" :msg msg)
      (recur (read-json in))))
  (log "Offline game has completed"))
