(ns punter.tcp
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [punter.util :refer [log]])
  (:import (java.net Socket)))

(defn connect [in-stream out-stream]
  (let [in (io/reader in-stream)
        out (io/writer out-stream)]
    {:in in :out out}))

(defn connect-online [host port]
  (let [socket (Socket. host port)
        in (.getInputStream socket)
        out (.getOutputStream socket)]
    (.setSoTimeout socket 10000) ; wait 10 sec for the game to start
    (connect in out)))

(defn write [conn msg]
  (println "sending message:" msg)
  (doto (:out conn)
    (.write msg 0 (count msg))
    (.flush)))

(defn- read-until
  [input-stream stop-symbol]
  (loop [string ""]
    (let [c (-> input-stream .read char)]
      (if (= c stop-symbol)
        string
        (recur (str string c))))))

(defn- read-symbols
  [input-stream amount]
  (loop [length 0
         string ""]
    (if (< length amount)
      (recur (+ 1 length)
             (str string (-> input-stream .read char)))
      string)))

(defn read-msg
  [input-stream]
  (let [length (-> (read-until input-stream \:)
                   read-string)
        msg (read-symbols input-stream length)]
    (log "tcp/read-msg:" msg)
    msg))

(defn readln [conn]
  (.readLine (:in conn)))
