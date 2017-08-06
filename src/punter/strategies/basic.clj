(ns punter.strategies.basic
  (require [punter.graph :as graph])
  (:import (punter.strategies.core StrategyProto)))

(defn- format
  "Post processing move"
  [{:keys [punter] :as <strategy>} move]
  {:state <strategy> :claim (assoc move :punter punter)})

(defn init*
  "Initializes strategy and returns it"
  [{:keys [punter punters map] :as game}]
  {:handled-moves 0 :my-moves 0 :punter punter :graph (graph/build game)})

(defn make-move
  "Updates internal state according to chosen move"
  [<strategy> move]
  (let [strategy (-> <strategy>
                     (update :my-moves inc)
                     (update :graph #(graph/claim % move)))]
    (format strategy move)))

(defn choose-best-first-move
  "Selects first move for given state"
  [{{:keys [mines] :as graph} :graph :as <strategy>}]
  (let [distances (->> (map #(hash-map :mine % :edges :distance 0) mines)
                       (remove #(= 0 (graph/free-degree graph (:mine %))))
                       (map #([(:mine %) %]))
                       (into {}))]
    (loop [[mine & rest] mines
           distances     distances]
      (if mine
        (recur rest (assoc-in distances [mine :distance] (reduce + (map #(-> graph :distances mine %) mines))))
        (let [mine (:mine (min-key :distance (vals distances)))])))))

(def choose-best-move
  "Selects next move for given state"
  [<strategy>])
  ; TODO

(defn sync-state
  "Syncs state with moves made by enemies"
  [{:keys [handled-moves] :as <strategy>} moves]
  (loop [[{{:keys [source target]} :claim pass :pass} & rest] (drop handled-moves moves)
         strategy                                             <strategy>]
    (if pass
      (recur rest strategy)
      (if source
        (recur rest (update strategy :graph #(graph/mark-as-busy % source target)))
        (assoc strategy :handled-moves moves)))))

(defn move*
  "Basic strategy"
  [{:keys [my-moves] :as <strategy>} moves]
  (let [strategy (sync-state <strategy> moves)
        move     (if (= 0 my-moves)
                   (choose-best-first-move strategy)
                   (choose-best-move strategy))]
    (make-move strategy move)))

(defrecord BasicStrategy []
  StrategyProto
  (init [game]
    (init* game))
  (move [<strategy> moves]
    (move* <strategy> moves)))

(defn ->make [] (->BasicStrategy))
