(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [punter.api :as api]
            [punter.tcp :as tcp]))

(def host "punter.inf.ed.ac.uk")
(def username "Lambda Riot")

(defn -main [& args]
  (println "starting Lambda Riot punter")
  (println "args:" args)
  (println "punter initialized")
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
  (println "connecting:" (str host ":" port))  
  (let [conn (tcp/connect host port)
        state (handshake conn)
        move (atom (api/recv-msg conn))
        _ (api/pass conn (:punter state))
        _ (println move)]
    (while true
      (let [next-move (api/recv-msg conn)
            punter (:punter state)]
        (println next-move)
        (swap! move next-move)
        (api/pass conn punter)))
    conn))
