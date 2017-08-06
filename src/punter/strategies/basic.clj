(ns punter.strategies.basic
  (require [punter.graph :as graph])
  (:import (punter.strategies.core StrategyProto)))

(defn- format
  "Post processing move"
  [{:keys [punter] :as <strategy>} source target]
  {:state <strategy> :claim {:punter punter :source source :target target}})

(defn init*
  "Initializes strategy and returns it"
  [{:keys [punter punters map] :as game}]
  {:last-step 0 :my-moves 0 :punter punter :graph (graph/build game)})

(defn move*
  "Basic strategy"
  [{:keys [punter] :as <strategy>} moves]
  ; TODO
  (format <strategy> source target))

(defrecord BasicStrategy []
  StrategyProto
  (init [game]
    (init* game))
  (move [<strategy> moves]
    (move* <strategy> moves)))

(defn ->make [] (->BasicStrategy))

