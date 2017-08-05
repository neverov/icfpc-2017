(ns punter.examples.game-states
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [punter.utils :as utils]))

(defn random-river
  [sites punters chance-of-owned]
  (when (> (count sites) 1)
    (let [start (rand-nth sites)]
      (loop [end (rand-nth sites)]
        (if (not= start end)
          (if-let [owner (utils/chance chance-of-owned (rand-nth punters))]
            (list (min start end) (max start end) owner)
            (list (min start end) (max start end)))
          (recur (rand-nth sites)))))))

(defn random-rivers
  ([sites]
   (random-rivers sites [nil] 0))
  ([sites punters]
   (random-rivers sites punters 0.3))
  ([sites punters chance-of-owned]
   (distinct (repeatedly
               (* (count sites) 3)
               #(random-river sites punters chance-of-owned)))))

(def game-state
  (let [punters [:jack :joe]
        punter :joe
        sites [1 2 3 4 5 6 7 8]
        mines [1 4]
        rivers [[5 7 :jack] [1 7 :joe] [1 5] [2 4]
                [6 8 :joe] [1 2] [2 3] [3 7]
                [4 8] [6 7] [3 6] [7 8]
                [1 4] [3 4] [2 8] [4 5 :joe]
                [4 7] [3 5 :jack] [1 7] [5 7]
                [5 8] [2 5] [3 8] [2 8 :joe]
                [2 7] [1 3] [3 6 :joe] [2 4 :joe]
                [4 5] [2 6] [3 5] [5 6]
                [3 5 :joe]]]
    {:punters punters
     :punter punter
     :sites sites
     :mines mines
     :rivers rivers
     :distance-maps (utils/distance-maps sites rivers mines)}))

(defn random-game-state
  ([total-sites punters]
   (random-game-state total-sites punters 0))
  ([total-sites punters progress]
   (let [punter (rand-nth punters)
         sites (range 1 (+ 1 total-sites))
         mines (take (count punters) (shuffle sites))
         rivers (random-rivers sites punters progress)]
     {:punters punters
      :punter punter
      :sites sites
      :mines mines
      :rivers rivers
      :distance-maps (utils/distance-maps sites rivers mines)})))
