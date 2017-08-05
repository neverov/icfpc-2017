(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]
            [punter.api :as api]
            [punter.util :refer [log]]))

(def server {:name "punter.inf.ed.ac.uk" :port 9002})

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

(defn handle-you [msg]
  (let [name (:you msg)]
    (println "got you:" name)))

(defn handle-init-state [conn msg]
  (let [punter (:punter msg)]
    (println "got init state")
    (api/ready conn punter)))

(defn session-handler [conn]
  (while (nil? (:exit conn))
    (when-let [line (.readLine (:in conn))]
      (let [payload (second (clojure.string/split line #":" 2))
            msg (parse-string payload true)]            
        (println "received message:" msg)
        (cond
          (you? msg) (handle-you msg)
          (init-state? msg) (handle-init-state conn msg) 
          :else (println "TBD"))))))
        
(defn connect [port]
  (let [server-map (assoc server :port port)
        _ (log "connecting:" server-map)  
        conn (tcp/connect server-map session-handler)]
    (api/init conn username)
    conn))
