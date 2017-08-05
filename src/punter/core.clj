(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]
            [punter.api :as api]
            [punter.util :refer [log]]))

(def host "punter.inf.ed.ac.uk")
(def username "Lambda Riot")

(defn -main [& args]
  (log "starting Lambda Riot punter")
  (log "args:" args)
  (log "punter initialized")
  (api/init nil username)
  (doseq [ln (line-seq (java.io.BufferedReader. *in*))]
    (println ln)))

(defn you? [msg]
  (contains? msg :you))

(defn init-state? [msg]
  (and (contains? msg :punter)
       (contains? msg :punters)
       (contains? msg :map)))

(defn stop? [msg]
  (contains? msg :stop))

(defn handshake [conn]
  (let [_ (api/init conn username)
        you (api/recv-you conn)
        _ (println "received you:" you)
        state (api/recv-state conn)
        _ (println "received initial game state:" state)
        _ (api/ready conn (:punter state))]
    state))

(defn play [port]
  (log "connecting:" (str host ":" port))  
  (let [conn (tcp/connect host port)
        state (handshake conn)
        move (atom (api/recv-msg conn))
        _ (api/pass conn)
        _ (println move)]
    (while (not (stop? @move))
      (let [next-move (api/recv-msg conn)]
        (println next-move)
        (swap! move next-move)
        (api/pass conn)))
    conn))
