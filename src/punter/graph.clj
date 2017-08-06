(ns punter.graph
  (:require [clojure.set :as set]
            [clojure.data.priority-map :refer priority-map]))

(def ^:private inf 10000000)

(declare adjacents)

(defn- add-edge
  [<graph> from to]
  (-> <graph>
      (update-in [:edges from] #(if % (conj % to) #{to}))
      (update-in [:edges   to] #(if % (conj % from) #{from}))))

(defn- handle-vertex
  [pq graph distances v]
  (loop [pq pq
         to (adjacents graph v)]
    (if to
      (let [old-distance (get distances to inf)
            new-distance (inc (get distances v))]
        (if (> old-distance new-distance)
          (recur (assoc pq to new-distance) (assoc distances to new-distance))
          (recur pq distances)))
      {:pq pq :distances distances})))

(defn- add-mine
  [<graph> mine]
  (loop [pq        (priority-map mine 0)
         distances (transient {mine 0})]
    (if (empty? pq)
      (->> distances
           (remove #(= %2 inf))
           (assoc-in <graph> [:distances mine]))
      (let [[v cur_d] (pop pq)]
        (if (> cur_d (get distances v inf))
          (recur pq distances)
          (let [{:keys [pq  distances]} (handle-vertex pq <graph> distances v)]
            (recur pq distances)))))))

(defn- add-rivers
  [<graph> rivers]
  (loop [{:keys [source target]} rivers
         graph                   <graph>]
    (if source
      (recur (rest rivers) (add-edge graph source target))
      graph)))

(defn- add-mines
  [<graph> mines]
  (loop [mine  mines
         graph <graph>]
    (if mine
      (recur (rest mines) (add-mine graph mine))
      graph))) 

(defn build
  "Builds graph from game's map"
  [{:keys [punter punters] {:keys [sites rivers mines]} :map}]
  (let [vertices (map :id sites)]
    (-> (transient {:mines (set mines) :edges {} :busy {} :allowed #{} :distances {}})
        (assoc :punters punters)
        (assoc :punter punter)
        (assoc :vertices vertices)
        (add-rivers rivers)
        (add-mines rivers)
        persistent!)))

(defn adjacents
  "Returns a set of adjacents vertices"
  [{:keys [edges]} vertex]
  (vertex edges))

(defn mark-as-busy
  "Marks edge as busy"
  [<graph> source target]
  (-> <graph>
      (update-in [:busy source] #(if % (conj % target) #{target}))
      (update-in [:busy target] #(if % (conj % source) #{source}))))

(defn claim
  "Claims an edge. Returns updated graph"
  [<graph> source target]
  (-> <graph>
      (mark-as-busy source target)
      (update :allowed #(set/union %
                                   (adjacents <graph> source)
                                   (adjacents <graph> target)))))

(defn busy?
  "Checks whether edge is busy"
  [{:keys [busy]} source target]
  (some-> busy source (contains? target)))

