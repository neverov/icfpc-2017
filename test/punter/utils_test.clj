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
            [2 6]))))

  (testing "should return sites reached by punter's rivers from the given site"
    (is (= #{1 4 3 2}
           (utils/punter-sites
            [1 2 3 4 5]
            [[1 2 :jack] [1 3 :jack] [2 4 :jack] [2 3] [4 5]]
            :jack
            1))))

  (testing "should return punter's score for a given mine"
    (let [sites [1 2 3 4 5]
          rivers [[1 2 :jack] [1 3 :jack] [2 4 :jack] [2 3] [4 5]]
          punter :jack
          mine 1
          distance-map (utils/distance-map sites rivers mine)
          owned-sites (utils/punter-sites sites rivers punter mine)]
      (is (= 6 (utils/punter-mine-score distance-map owned-sites)))))

  (testing "should calculate current score for punter"
    (let [sites [1 2 3 4 5 6 7]
          rivers [[1 2 :jack] [1 3 :jack] [2 4 :jack] [2 3] [4 5] [6 7 :jack] [5 7 :jack]]
          mines [1 5]
          distance-maps (utils/distance-maps sites rivers mines)
          punter :jack]
      (is (= 11
             (utils/punter-total-score
              {:sites sites
               :rivers rivers
               :mines mines
               :distance-maps distance-maps}
              punter)))))

  (testing "should calculate total scoreboard"
    (let [sites [0 1 2 3 4 5 6 7]
          rivers [[0 1 :bob] [0 7 :bob] [1 2 :alice] [1 3 :alice]
                  [1 7 :alice] [2 3 :bob] [3 4 :alice] [3 5 :bob]
                  [4 5 :alice] [5 6 :bob] [5 7 :alice] [6 7 :bob]]
          mines [1 5]
          distance-maps (utils/distance-maps sites rivers mines)
          punters [:alice :bob]]
      (is (= {:scores [{:punter :alice, :score 22} {:punter :bob, :score 27}]}
             (utils/scoreboard
              {:sites sites
               :rivers rivers
               :mines mines
               :distance-maps distance-maps
               :punters punters}))))))
