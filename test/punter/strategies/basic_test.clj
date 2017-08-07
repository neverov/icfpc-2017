(ns punter.strategies.basic-test
  (:require [punter.strategies.basic :as basic]
            [punter.strategies.core :as core]
            [clojure.test :refer :all]
            [punter.graph :as graph])
  (:import (punter.strategies.basic BasicStrategy)))

(def game
  {:map {:sites [{:id 0, :x 0.0, :y 0.0} {:id 1, :x 1.0, :y 0.0}
                 {:id 2, :x 2.0, :y 0.0} {:id 3, :x 2.0, :y -1.0}
                 {:id 4, :x 2.0, :y -2.0} {:id 5, :x 1.0, :y -2.0}
                 {:id 6, :x 0.0, :y -2.0} {:id 7, :x 0.0, :y -1.0}],
         :rivers [{:source 0, :target 1} {:source 1, :target 2}
                  {:source 0, :target 7} {:source 7, :target 6}
                  {:source 6, :target 5} {:source 5, :target 4}
                  {:source 4, :target 3} {:source 3, :target 2}
                  {:source 1, :target 7} {:source 1, :target 3}
                  {:source 7, :target 5} {:source 5, :target 3}],
         :mines [1 5]}
   :punter 0
   :punters 2})

(def complicated-game
  {:punter 0
   :punters 2
   :map {:sites [{:id 0, :x 0.5, :y 0.0} {:id 1, :x -0.5, :y 0.0}
                 {:id 2, :x 0.0, :y 1.0} {:id 3, :x 0.0, :y -1.0}
                 {:id 4, :x -1.0, :y -1.0} {:id 5, :x -0.5, :y -2.0}
                 {:id 6, :x 0.5, :y 2.0} {:id 7, :x 1.0, :y 1.0}
                 {:id 8, :x -1.0, :y 1.0} {:id 9, :x -0.5, :y 2.0}
                 {:id 10, :x 1.5, :y 2.0} {:id 11, :x -1.5, :y 2.0}
                 {:id 12, :x -1.0, :y 3.0} {:id 13, :x 1.0, :y 3.0}
                 {:id 14, :x -2.0, :y 3.0} {:id 15, :x 2.0, :y 3.0}
                 {:id 16, :x -1.5, :y -2.0} {:id 17, :x -1.0, :y -3.0}
                 {:id 18, :x -0.75, :y -1.5} {:id 19, :x -0.75, :y -2.5}
                 {:id 20, :x -1.25, :y -2.5} {:id 21, :x -1.25, :y -1.5}
                 {:id 22, :x -1.0, :y -2.0} {:id 23, :x 0.25, :y 0.5}
                 {:id 24, :x 0.25, :y -0.5} {:id 25, :x -0.25, :y -0.5}
                 {:id 26, :x -0.25, :y 0.5} {:id 27, :x 0.0, :y 0.0}
                 {:id 28, :x -0.75, :y 2.5} {:id 29, :x -0.75, :y 1.5}
                 {:id 30, :x -1.25, :y 1.5} {:id 31, :x -1.25, :y 2.5}
                 {:id 32, :x -1.0, :y 2.0} {:id 33, :x 1.25, :y 2.5}
                 {:id 34, :x 1.25, :y 1.5} {:id 35, :x 0.75, :y 1.5}
                 {:id 36, :x 0.75, :y 2.5} {:id 37, :x 1.0, :y 2.0}],
         :rivers [{:source 23, :target 27} {:source 26, :target 27}
                  {:source 3, :target 5} {:source 11, :target 14}
                  {:source 24, :target 27} {:source 19, :target 22}
                  {:source 20, :target 22} {:source 1, :target 4}
                  {:source 0, :target 7} {:source 21, :target 22}
                  {:source 2, :target 4} {:source 2, :target 6}
                  {:source 30, :target 32} {:source 29, :target 32}
                  {:source 31, :target 32} {:source 10, :target 15}
                  {:source 28, :target 32} {:source 1, :target 8}
                  {:source 2, :target 9} {:source 25, :target 27}
                  {:source 4, :target 18} {:source 5, :target 18}
                  {:source 5, :target 19} {:source 17, :target 19}
                  {:source 17, :target 20} {:source 16, :target 20}
                  {:source 16, :target 21} {:source 4, :target 21}
                  {:source 18, :target 22} {:source 2, :target 23}
                  {:source 0, :target 23} {:source 0, :target 24}
                  {:source 3, :target 24} {:source 3, :target 25}
                  {:source 1, :target 25} {:source 1, :target 26}
                  {:source 2, :target 26} {:source 12, :target 28}
                  {:source 9, :target 28} {:source 9, :target 29}
                  {:source 8, :target 29} {:source 8, :target 30}
                  {:source 11, :target 30} {:source 11, :target 31}
                  {:source 12, :target 31} {:source 13, :target 33}
                  {:source 10, :target 33} {:source 10, :target 34}
                  {:source 7, :target 34} {:source 7, :target 35}
                  {:source 6, :target 35} {:source 6, :target 36}
                  {:source 13, :target 36} {:source 33, :target 37}
                  {:source 34, :target 37} {:source 35, :target 37}
                  {:source 36, :target 37} {:source 23, :target 35}
                  {:source 18, :target 25} {:source 26, :target 29}],
         :mines [27 32 37 22]}})

(defn strategy
  ([]
   (strategy game))
  ([game]
   (-> (basic/->make) (core/init game))))

(deftest make-move-test
  (let [strategy                     (strategy)
        move                         {:source 1 :target 2}
        {strategy :state :as answer} (basic/make-move strategy move)]

    (is (= {:claim (assoc move :punter 0)}  (dissoc answer :state)))
    (is (= 1 (:my-moves strategy)))
    (is (graph/busy? (:graph strategy) 1 2))))

(deftest sync-state-test
  (let [strategy   (strategy)
        state-1    {:claim {:punter 1 :source 0, :target 1}}
        state-2    {:claim {:punter 1 :source 1, :target 2}}
        strategy-1 (basic/sync-state strategy {:moves [state-1]})
        strategy-2 (basic/sync-state strategy {:moves [state-1 state-2]})]

    (is (graph/busy? (:graph strategy-1) 0 1))
    (is (not (graph/busy? (:graph strategy-1) 1 2)))
    (is (graph/busy? (:graph strategy-2) 1 2))
    (is (= 1 (:handled-moves strategy-1)))
    (is (= 2 (:handled-moves strategy-2)))))

(deftest choose-best-first-move-test
  (let [strategy (strategy complicated-game)]
    (is (= {:source 27, :target 24} (basic/choose-best-first-move strategy)))))

(deftest choose-best-move-test
  (let [strategy (-> (strategy complicated-game)
                     (basic/make-move {:source 27, :target 24})
                     :state)]
    (is (= {:source 0, :target 24} (basic/choose-best-move strategy)))))
