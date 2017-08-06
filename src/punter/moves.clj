(ns punter.moves
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.set :as set]))

(defn pass
  [punter-id]
  {:pass {:punter punter-id}})

(defn claim
  [punter-id [river-from river-to]]
  {:claim {:punter punter-id
           :source (min river-from river-to)
           :target (max river-from river-to)}})

(defn ->fixed-claim
  [claim]
  (let [data (:claim claim)
        {:keys [source target]} data]
    (if (> source target)
      {:claim (assoc data :source target :target source)}
      claim)))

(defn splurge
  [punter-id sites]
  {:splurge {:punter punter-id :route sites}})

(defn splurge->claims
  [splurge]
  (let [data (:splurge splurge)
        {punter-id :punter sites :route} data
        river-starts (butlast sites)
        river-ends (rest sites)
        rivers (partition 2 (interleave river-starts river-ends))
        ->claim (fn [river] (claim punter-id river))]
    (map ->claim rivers)))
