(ns punter.strategies.basic
  (require [punter.graph :as graph]
           [punter.strategies.core :refer [StrategyProto]]))

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
  [<strategy> {:keys [source target] :as move}]
  (let [strategy (-> <strategy>
                     (update :my-moves inc)
                     (update :graph #(graph/claim % source target)))]
    (format strategy move)))

(defn choose-best-first-move
  "Selects first move for given state"
  [{{:keys [mines] :as graph} :graph :as <strategy>}]
  (let [distances (->> (map #(hash-map :mine % :distance 0) mines)
                       (map #(assoc % :edges (graph/free-edges graph (:mine %))))
                       (remove #(= 0 (graph/free-degree graph (:mine %))))
                       (map #(vector (:mine %) %))
                       (into {}))]
    (loop [[mine & rest] (seq mines)
           distances     distances]
      (if mine
        (recur rest (assoc-in distances [mine :distance] (reduce + (map #(graph/distance graph mine %) mines))))
        (let [{:keys [mine edges]} (apply min-key :distance (vals distances))]
          {:source mine :target (first edges)})))))

(defn- score-for-vertex
  [{:keys [owned-mines] :as graph} vertex]
  (->> (map #(graph/distance graph % vertex) owned-mines)
       (map #(* % %))
       (reduce +)))

(defn choose-best-move
  "Selects next move for given state"
  [{{:keys [allowed] :as graph} :graph}]
  (loop [[[source target] & rest] allowed
         best-move     {:score 0}]
    (if source
      (if (graph/busy? graph source target)
        (recur rest best-move)
        (let [score (score-for-vertex graph target)]
          (if (> score (:score best-move))
            (recur rest {:score score :source source :target target})
            (recur rest best-move))))
      (dissoc best-move :score))))

(defn sync-state
  "Syncs state with moves made by enemies"
  [{:keys [handled-moves] me :punter :as <strategy>} {:keys [moves]}]
  (loop [[{{:keys [source target punter] :as claim} :claim pass :pass} & rest] (drop handled-moves moves)
         strategy <strategy>]
    (if (or pass (= punter me))
      (recur rest strategy)
      (if claim
        (recur rest (update strategy :graph #(graph/mark-as-busy % source target)))
        (assoc strategy :handled-moves (count moves))))))

(defn move*
  "Basic strategy"
  [{:keys [my-moves] :as <strategy>} moves]
  (clojure.pprint/pprint moves)
  (let [strategy (sync-state <strategy> moves)
        move     (if (= 0 my-moves)
                   (choose-best-first-move strategy)
                   (choose-best-move strategy))]
    (make-move strategy move)))

(defrecord BasicStrategy []
  StrategyProto
  (init [_ game]
    (init* game))
  (move [this moves]
    (move* this moves)))

(defn ->make [] (->BasicStrategy))
