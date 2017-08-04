(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]))

(def server {:name "punter.inf.ed.ac.uk" :port 9010})

(def username "Lambda Riot")

(defn -main
  [raw-state & args]
  (println "raw state:" raw-state)
  (let [state (parse-string raw-state true)]
    (println "get state:" state)))

(defn connect []
  (tcp/connect server tcp/print-handler))

;; api

(defn greet [ch name]
  (let [msg (generate-string {:me name})] 
    (tcp/write ch msg)))  
