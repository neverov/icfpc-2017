(ns punter.strategies.core)

(defprotocol StrategyProto
  (init [game])
  (move [<strategy> moves]))