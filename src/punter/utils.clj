(ns punter.utils
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.set :as set]))

(defn adjacent-sites
  "given a set of sites and rivers
  returns a set of sites, that are adjacent to given ones, but not contained in them.
  e.g. [#{1 2 3} [[1 5] [1 3] [1 4]]] -> #{4 5}"
  [sites rivers]
  (->> (for [r rivers
             :let [river (set r)
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
