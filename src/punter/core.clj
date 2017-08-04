(ns punter.core
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]
            [punter.api :as api]))

(def server {:name "punter.inf.ed.ac.uk" :port 9001})

(def username "Lambda Riot")

(defn -main
  [raw-state & args]
  (println "raw state:" raw-state)
  (let [state (parse-string raw-state true)]
    (println "get state:" state)))

(defn connect []
  (let [c (tcp/connect server tcp/print-handler)]
    (api/init c username)
    c))
