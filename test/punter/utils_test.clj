(ns punter.utils-test
  (:require [clojure.test :refer :all]
            [punter.utils :as utils]))

(deftest distance-maps
  (testing "should calculate adajcent sites"
    (is (= #{2} (utils/adjacent-sites #{1} [[1 2]])))
    (is (= #{} (utils/adjacent-sites #{5} [[1 2] [1 3] [1 4]])))
    (is (= #{1} (utils/adjacent-sites #{3} [[1 2] [1 3] [1 4]])))
    (is (= #{2 3 4} (utils/adjacent-sites #{1} [[1 2] [1 3] [1 4]])))
    (is (= #{4 5} (utils/adjacent-sites #{1 2 3} [[1 5] [1 3] [1 4]])))
    (is (= #{1} (utils/adjacent-sites #{5 3} [[1 5] [1 3] [1 4]]))))

  (testing "should calculate distance map"
    (is (= {:1 1 :2 0 :3 2 :4 1 :5 2 :6 nil}
           (utils/distance-map
            [:1 :2 :3 :4 :5 :6]
            [[:1 :2] [:1 :3] [:2 :4] [:1 :3] [:4 :5]]
            :2))))

  (testing "should calculate all distance maps"
    (is (= {2 {1 1
               2 0
               3 2
               4 1
               5 2
               6 nil
               7 nil
               8 nil
               9 nil}
            6 {1 nil
               2 nil
               3 nil
               4 nil
               5 nil
               6 0
               7 1
               8 2
               9 1}}
           (utils/distance-maps
            [1 2 3 4 5 6 7 8 9]
            [[1 2] [1 3] [2 4] [1 3] [4 5] [6 7] [7 8] [6 9]]
            [2 6])))))
