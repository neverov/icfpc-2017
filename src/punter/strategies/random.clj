(ns punter.strategies.random
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]))

(defn move
  "random strategy: capture random vacant river"
  [game-state]
  (let [{:keys [sites mines rivers punter punters distance-maps meta]} game-state
        vacant-rivers (filter #(nil? (get % 2)) rivers)
        target-river (take 2 (rand-nth vacant-rivers))]
    {:move {:claim {:punter punter :source (first target-river) :target (last target-river)}}}))
