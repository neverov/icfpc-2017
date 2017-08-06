(ns punter.strategies.basic
  (:import (punter.strategies.core StrategyProto)))

(defn- format
  "Post processing move"
  [{:keys [punter] :as <strategy>} source target]
  {:state <strategy> :claim {:punter punter :source source :target target}})

(defn init*
  "Initializes strategy and returns it"
  [{:keys [punter punters map]}])

(defn move*
  "Basic strategy"
  [{:keys [punter] :as <strategy>} moves]
  ; TODO
  (format <strategy> source target))
  ;(let [{:keys [punter punters sites mines rivers]} game-state
  ;      vacant-rivers (filter #(nil? (get % 2)) rivers)
  ;      target-river (take 2 (rand-nth vacant-rivers))]
  ;  {:claim {:punter punter :source (first target-river) :target (last target-river)}}))

(defrecord BasicStrategy []
  StrategyProto
  (init [game]
    (init* game))
  (move [<strategy> moves]
    (move* <strategy> moves)))

(defn ->make [] (->BasicStrategy))

