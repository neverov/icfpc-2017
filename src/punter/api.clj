(ns punter.api
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]] 
            [cheshire.core :refer [generate-string parse-string]]
            [punter.tcp :as tcp]))

(defn init [ch name]
  (let [payload {:me name} 
        msg (generate-string payload)]
    (tcp/write ch msg)))  

(defn move [ch punter source target]
  (let [payload {:claim {:punter punter :source source :target target}}
        msg (generate-string payload)]
    (tcp/write ch msg)))

(defn pass [ch punter]
  (let [payload {:pass {:punter punter}}
        msg (generate-string payload)]
    (tcp/write ch msg)))
