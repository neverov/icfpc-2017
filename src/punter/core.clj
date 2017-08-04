(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]
            [punter.api :as api]))

(def server {:name "punter.inf.ed.ac.uk" :port 9002})

(def username "Lambda Riot")

(defn -main
  [raw-state & args]
  (println "raw state:" raw-state)
  (let [state (parse-string raw-state true)]
    (println "get state:" state)))

(defn session-handler [conn]
  (while (nil? (:exit conn))
    (when-let [line (.readLine (:in conn))]
      (println "got line:" line)
      (let [payload (second (clojure.string/split line #":" 2))
            _ (println payload)
            msg (parse-string payload true)]
        (println "received message:" msg)))))

(defn connect []
  (let [c (tcp/connect server session-handler)]
    (api/init c username)
    c))