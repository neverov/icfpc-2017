(ns punter.graph-test
  (:require [clojure.test :refer :all]
            [punter.graph :as graph]))

(deftest adjacents-test
  (let [graph (-> {}
                  (#'graph/add-edge 1 2)
                  (#'graph/add-edge 2 3)
                  (->> (assoc {} :edges)))]
    (is (= #{2}   (graph/adjacents graph 1)))
    (is (= #{1 3} (graph/adjacents graph 2)))
    (is (= #{2}   (graph/adjacents graph 3)))))

(deftest busy?-test
  (let [graph (-> {}
                  (#'graph/add-edge 1 2)
                  (#'graph/add-edge 2 3)
                  (->> (assoc {} :edges))
                  (graph/mark-as-busy 1 2))]
    (is (graph/busy? graph 1 2))
    (is (graph/busy? graph 2 1))
    (is (not (graph/busy? graph 2 3)))
    (is (not (graph/busy? graph 3 2)))
    (is (not (graph/busy? graph 1 3)))
    (is (not (graph/busy? graph 4 6)))
    (is (not (graph/busy? graph 1 6)))))

(deftest busy?-test
  (let [graph (-> {}
                  (#'graph/add-edge 1 2)
                  (#'graph/add-edge 2 3)
                  (->> (assoc {} :edges)))]
    (is (not (graph/busy? graph 1 2)))
    (let [graph (graph/mark-as-busy graph 1 2)]
      (is (graph/busy? graph 1 2))
      (is (graph/busy? graph 2 1)))))

(deftest free-degree-test
  (let [graph (-> {}
                  (#'graph/add-edge 1 2)
                  (#'graph/add-edge 2 3)
                  (->> (assoc {} :edges)))]
    (is (= 2 (graph/free-degree graph 2)))

    (let [graph (graph/claim graph 1 2)]
      (is (= 1 (graph/free-degree graph 2))))))

(deftest claim-test
  (let [graph (-> {}
                  (#'graph/add-edge 1 2)
                  (#'graph/add-edge 2 3)
                  (#'graph/add-edge 1 4)
                  (->> (assoc {:allowed #{}} :edges)))]
    (is (= 2 (graph/free-degree graph 2)))
    (is (= #{} (:allowed graph)))
    (let [graph (graph/claim graph 1 2)]
      (is (= 1 (graph/free-degree graph 2)))
      (is (= #{1 2 3 4} (:allowed graph))))))

(deftest build-test
  (let [map' {:sites [{:id 0, :x 0.0, :y 0.0} {:id 1, :x 1.0, :y 0.0}
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
        graph (graph/build {:punter 0 :punters [2] :map map'})]
    (is (= #{1 5}         (:mines graph)))
    (is (= {}             (:busy graph)))
    (is (= #{}            (:allowed graph)))
    (is (= {0 #{7 1},
            1 #{0 7 3 2},
            2 #{1 3},
            7 #{0 1 6 5},
            6 #{7 5},
            5 #{7 4 6 3},
            4 #{3 5},
            3 #{1 4 2 5}} (:edges graph)))
    (is (= {1 {1 0, 0 1, 7 1, 3 1, 2 1, 6 2, 5 2, 4 2},
            5 {5 0, 7 1, 4 1, 6 1, 3 1, 0 2, 1 2, 2 2}} (:distances graph)))))