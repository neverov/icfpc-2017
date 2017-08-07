(ns punter.strategies.core)

(defprotocol StrategyProto
  (init [this game])
  (move [this moves]))