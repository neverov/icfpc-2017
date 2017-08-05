(ns punter.strategies.shy
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]))

(defn move
  "shy strategy: always pass"
  [game-state]
  (let [{:keys [sites mines rivers punter punters distance-maps meta]} game-state]
    {:pass {:punter punter}}))
