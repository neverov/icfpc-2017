(ns punter.strategies.random
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]))

(defn move
  "random strategy: capture random vacant river"
  [game-state]
  (let [{:keys [punter punters map]} game-state
        {:keys [sites mines  rivers]} map
        vacant-rivers (filter #(nil? (get % 2)) rivers)
        target-river (take 2 (rand-nth vacant-rivers))]
    {:move {:claim {:punter punter :source (first target-river) :target (last target-river)}}}))
