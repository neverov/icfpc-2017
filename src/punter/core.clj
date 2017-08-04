(ns punter.core
  (:gen-class)
  (:require [cheshire.core :refer [parse-string]]))

(defn -main
  [raw-state & args]
  (println "raw state:" raw-state)
  (let [state (parse-string raw-state true)]
    (println "get state:" state)))
