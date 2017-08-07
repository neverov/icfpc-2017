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
  (log "sending message:" msg)
  (doto (:out conn)
    (.write msg 0 (count msg))
    (.flush)))

(defn readln [conn]
  (.readLine (:in conn)))

(defn readln-t [conn]
  (let [in (:in conn)
        buffer (atom "")
        length (atom 0)
        ch (atom 0)]
    (reset! ch (.read in))
    (swap! buffer conj @ch)
    (while (not= @ch ':')
      (reset! ch (.read in))
      (swap! buffer conj @ch))
    (reset! length (int @buffer))
    (reset! buffer "")
    (dotimes [n @length]
      (reset! ch (.read in))
      (swap! buffer conj @ch))
    @buffer))

(defn test-tt []
  (let [in *in*]
    (log *in*)
    (log (slurp *in*))
    (log (.read in))
    (while true
      (let [ch (.read in)]
        (if (= ch ':')
          ;(dotimes [_ (int @buffer)])

          (log "read:" ch))))))
        ;(swap! buffer conj ch)
        ;(log @buffer)))))

