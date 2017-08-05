(ns punter.utils
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.set :as set]))

(defn chance
  "returns the item with a given chance, otherwise nil"
  [chance item]
  (when (< (rand) chance)
    item))

(defn ->game-state
  "transforms initial game state to a shiny sweet narrow form"
  [state]
  (let [{punter :punter punters :punters game-map :map} state
        {:keys [sites mines rivers]} game-map
        punters (range punters)
        sites (map :id sites)
        rivers (map (fn [{:keys [source target] :as river}]
                      [(min source target) (max source target)])
                    rivers)
        dist-maps (distance-maps sites rivers mines)]
    {:punter punter
     :punters punters
     :sites sites
     :mines mines
     :rivers rivers
     :distance-maps dist-maps}))

(defn apply-move
  "applies a list of moves to game state"
  [state move]
  (let [moves (->> move :move :moves
                   (remove :pass)
                   (map :claim))
        rivers (:rivers state)
        updated-rivers (reduce
                         (fn [rivers claim]
                           (let [{:keys [source target punter]} claim
                                 rivers (vec rivers)
                                 river [(min source target) (max source target)]
                                 claimed-river (conj river punter)]
                             (-> (remove #(= % river) rivers)
                                 (conj claimed-river))))
                         rivers
                         moves)]
    (assoc state :rivers updated-rivers)))

(defn adjacent-sites
  "given a set of sites and rivers
  returns a set of sites, that are adjacent to given ones, but not contained in them.
  e.g. [#{1 2 3} [[1 5] [1 3] [1 4]]] -> #{4 5}"
  [sites rivers]
  (->> (for [r rivers
             :let [river (set (take 2 r))
                   intersection (set/intersection sites river)]]
         (when (not-empty intersection)
           (first (set/difference river sites))))
       (remove nil?)
       set))

(defn distance-map
  "given sites as [1 2 3 4 5 6]
  rivers as [[1 2] [1 3] [2 4] [1 3] [4 5]]
  and starting point 2
  returns a list of distances to every node
  {1 1, 2 0, 3 2, 4 1, 5 2, 6 nil]}
  "
  [sites rivers start-node]
  (let [distances (apply assoc {} (interleave sites (repeat (count sites) nil)))
        distances (assoc distances start-node 0)]
    (loop [cost 1
           distances distances
           covered-sites #{start-node}]
      (let [adjs (adjacent-sites covered-sites rivers)]
        (if (not-empty adjs)
          (recur (+ 1 cost)
                 (apply assoc distances (interleave adjs (repeat (count adjs) cost)))
                 (set/union covered-sites adjs))
          distances)))))

(defn distance-maps
  "given sites as [1 2 3 4 5 6 7 8 9]
  rivers as [[1 2] [1 3] [2 4] [1 3] [4 5] [6 7] [7 8] [6 9]]
  and mines as [2 6]
  returns a list of distances to every node
  {2 {1 1, 2 0, 3 2, 4 1, 5 2, 6 nil, 7 nil, 8 nil, 9 nil]}
   6 {1 nil, 2 nil, 3 nil, 4 nil, 5 nil, 6 0, 7 1, 8 2, 9 1]}}
  "
  [sites rivers mines]
  (into {} (for [mine mines
                 :let [dmap (distance-map sites rivers mine)]]
             [mine dmap])))

(defn punter-sites
  "given a set of sites and rivers, starting site and punter-id
  returns a set of sites, connected to given site via punter-owned rivers"
  [sites rivers punter start-site]
  (let [owned-rivers (filter #(= punter (get % 2)) rivers)]
    (loop [covered-sites #{start-site}]
      (let [adjs (adjacent-sites covered-sites owned-rivers)]
        (if (empty? adjs)
          covered-sites
          (recur (set/union covered-sites adjs)))))))

(defn punter-mine-score
  "given a set of connected sites and distance map for the mine,
  calculate punters score"
  [distance-map sites]
  (when-not (empty? sites)
    (->> sites
      (map #(get distance-map %))
      (map #(* % %))
      (reduce +))))

(defn punter-total-score
  "given a game state and punter id,
  calculate punters score"
  [{:keys [distance-maps sites rivers mines] :as game-state} punter-id]
  (let [owned-network (->> mines
                           (map (fn [mine]
                                  [mine (punter-sites sites rivers punter-id mine)])))
        network-scores (->> owned-network
                            (map (fn [[mine sites]]
                                   (punter-mine-score
                                     (get distance-maps mine)
                                     sites))))]
    (reduce + 0 network-scores)))

(defn scoreboard
  "given a game state calculate scores for all punters"
  [{:keys [distance-maps sites rivers mines punters] :as game-state}]
  (let [scores (->> punters
                    (map #(do [% (punter-total-score game-state %)]))
                    (into {}))]
    {:scores (reduce-kv (fn [coll punter score]
                          (conj coll {:punter punter :score score}))
                        []
                        scores)}))
