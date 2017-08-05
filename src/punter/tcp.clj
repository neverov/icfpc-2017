(ns punter.tcp
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io])
  (:import (java.net Socket)))

(defn print-handler [conn]
  (while (nil? (:exit conn))
    (if-let [msg (.readLine (:in conn))]
      (println "received raw message:" msg))))

(defn connect [host port]
  (let [socket (Socket. host port)
        in (io/reader (.getInputStream socket))
        out (io/writer (.getOutputStream socket))]
    {:in in :out out}))

(defn write [conn msg]
  (println "sending message:" msg)
  (doto (:out conn)
    (.write msg 0 (count msg))
    (.flush)))

(defn read-line [conn]
  (.readLine (:in conn)))
