(ns punter.tcp
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io])
  (:import (java.net Socket)))

(defn connect [in-stream out-stream]
  (let [in (io/reader in-stream)
        out (io/writer out-stream)]
    {:in in :out out}))

(defn connect-online [host port]
  (let [socket (Socket. host port)
        in (.getInputStream socket)
        out (.getOutputStream socket)]
    (.setSoTimeout socket 100000) ; wait 10 sec for the game to start
    (connect in out)))        

(defn write [conn msg]
  (println "sending message:" msg)
  (doto (:out conn)
    (.write msg 0 (count msg))
    (.flush)))

(defn read-line [conn]
  (.readLine (:in conn)))
