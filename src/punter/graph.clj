(ns punter.graph)

(defn build
  "Build graph from game's map"
  [{:keys [punter punters] {:keys [sites rivers mines]} :map}])
  ; TODO

(defn adjacents
  "Returns adjacents vertices"
  [{:keys [edges]} vertex]
  (vertex edges))

(defn mark-as-busy
  "Marks edge as busy"
  [<graph> source target])
  ; TODO

(defn claim
  "Claims an edge. Returns updated graph"
  [<graph> source target]
  (let [<graph> (mark-as-busy <graph> source target)]))
    ; TODO

(defn busy?
  "Checks whether edge is busy"
  [{:keys [busy]} source target]
  (-> busy source (contains? target)))
