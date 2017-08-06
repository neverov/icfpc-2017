(ns punter.graph
  (:require [clojure.set :as set]
            [clojure.data.priority-map :refer [priority-map]]))

(def ^:private inf 10000000)

(declare adjacents)

(defn- add-edge
  "Adds an edge"
  [edges from to]
  (-> edges
      (update from #(if % (conj % to) #{to}))
      (update to   #(if % (conj % from) #{from}))))

(defn- compute-edges
  "Converts rivers to edges"
  [rivers]
  (loop [[{:keys [source target]} & rest] rivers
         edges                            {}]
    (if source
      (recur rest (add-edge edges source target))
      edges)))

(defn- handle-vertex
  [pq graph distances v]
  (loop [pq        pq
         distances distances
         adj       (adjacents graph v)]
    (let [to   (first adj)
          rest (disj adj to)]
      (if to
        (let [old-distance (get distances to inf)
              new-distance (inc (get distances v))]
          (if (> old-distance new-distance)
            (recur (assoc pq to new-distance) (assoc! distances to new-distance) rest)
            (recur pq distances rest)))
        {:pq pq :distances distances}))))

(defn- compute-mine
  "Computes distances from `mine` to all vertices"
  [<graph> mine]
  (loop [pq        (priority-map mine 0)
         distances (transient {mine 0})]
    (if (empty? pq)
      (persistent! distances)
      (let [[v cur-d] (peek pq)
            pq        (pop pq)]
        (if (> cur-d (get distances v inf))
          (recur pq distances)
          (let [{:keys [pq distances]} (handle-vertex pq <graph> distances v)]
            (recur pq distances)))))))

(defn- compute-distances
  "Computes distances from all mines to all vertices"
  [<graph> mines]
  (loop [[mine & rest] mines
         distances     (transient {})]
    (if mine
      (recur rest (assoc! distances mine (compute-mine <graph> mine)))
      (persistent! distances))))

(defn build
  "Builds graph from game's map"
  [{:keys [punter punters] {:keys [sites rivers mines]} :map}]
  (let [vertices (map :id sites)
        graph (-> (transient {:mines (set mines) :edges {} :busy {} :allowed #{} :distances {}})
                  (assoc! :punters punters)
                  (assoc! :punter punter)
                  (assoc! :vertices vertices)
                  (assoc! :edges (compute-edges rivers)))
        distances (compute-distances graph mines)]
    (-> (assoc! graph :distances distances)
        persistent!)))

(defn adjacents
  "Returns a set of adjacents vertices"
  [{:keys [edges]} vertex]
  (get edges vertex))

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
  (some-> (get busy source) (contains? target)))

(defn free-degree
  "How many free edges come into vertex. 0 means the vertex is busy"
  [<graph> vertex]
  (- (count (adjacents <graph> vertex))
     (count (get-in <graph> [:busy vertex] #{}))))
