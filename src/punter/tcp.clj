(ns punter.tcp
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
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
  (log "sending message:" msg)
  (doto (:out conn)
    (.write msg 0 (count msg))
    (.flush)))

(defn- read-until
  [input-stream stop-symbol]
  (loop [string ""]
    (let [c (-> input-stream .read)]
      (cond
        (= c -1) (recur string)
        (= (char c) stop-symbol) string
        :else (recur (str string (char c)))))))

(defn- read-symbols
  [input-stream amount]
  (loop [length 0
         string ""]
    (if (< length amount)
      (recur (inc length)
             (str string (-> input-stream .read char)))
      string)))

(defn read-msg
  [conn]
  (let [in (:in conn)
        length (-> (read-until in \:)
                   read-string)
        msg (read-symbols in length)]
    msg))

(defn read-json
  [conn]
  ;; Throw away the length leader
  (while (not= \: (char (.read (:in conn)))))
  (let [val (json/read (:in conn))]
    (log "read:" (pr-str val))
    val))
