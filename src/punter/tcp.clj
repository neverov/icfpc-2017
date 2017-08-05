(ns punter.tcp
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io])
  (:import (java.net Socket)))

(defn connect [host port]
  (let [socket (Socket. host port)
        in (io/reader (.getInputStream socket))
        out (io/writer (.getOutputStream socket))]
    (.setSoTimeout socket 10000) ; wait 10 sec for the game to start
    {:in in :out out}))

(defn write [conn msg]
  (println "sending message:" msg)
  (doto (:out conn)
    (.write msg 0 (count msg))
    (.flush)))

(defn read-line [conn]
  (.readLine (:in conn)))
