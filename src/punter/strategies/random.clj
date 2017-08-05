(ns punter.strategies.random
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]))

(defn move
  "random strategy: capture random vacant river"
  [game-state]
  (let [{:keys [punter punters sites mines rivers]} game-state
        vacant-rivers (filter #(nil? (get % 2)) rivers)
        target-river (take 2 (rand-nth vacant-rivers))]
    {:claim {:punter punter :source (first target-river) :target (last target-river)}}))
