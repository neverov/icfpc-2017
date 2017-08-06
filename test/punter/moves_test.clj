(ns punter.moves-test
  (:require [clojure.test :refer :all]
            [punter.moves :as moves]))

(deftest moves
  (testing "should build pass move"
    (is (= {:pass {:punter 1}} (moves/pass 1))))

  (testing "should build claim move"
    (is (= {:claim {:punter 1 :source 4 :target 5}} (moves/claim 1 [4 5])))
    (is (= {:claim {:punter 1 :source 4 :target 5}} (moves/claim 1 [5 4]))))

  (testing "should fix misordered claim move"
    (is (= {:claim {:punter 1 :source 4 :target 5}}
           (moves/->fixed-claim {:claim {:punter 1 :source 4 :target 5}})))
    (is (= {:claim {:punter 1 :source 4 :target 5}}
           (moves/->fixed-claim {:claim {:punter 1 :source 5 :target 4}}))))

  (testing "should build splurge move"
    (is (= {:splurge {:punter 1 :route [4 5 6]}}
           (moves/splurge 1 [4 5 6]))))

  (testing "should destructure splurge move into claims"
    (is (= [{:claim {:punter 1 :source 2 :target 3}}
            {:claim {:punter 1 :source 2 :target 5}}
            {:claim {:punter 1 :source 4 :target 5}}]
           (moves/splurge->claims
             (moves/splurge 1 [3 2 5 4]))))))
