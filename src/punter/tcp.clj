(ns punter.tcp
  (:gen-class)
  (:require [clojure.tools.namespace.repl :refer [refresh]])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(defn socket [params]
  (Socket. (:name params) (:port params)))

(defn in [socket]
  (BufferedReader. (InputStreamReader. (.getInputStream socket))))

(defn out [socket]
  (PrintWriter. (.getOutputStream socket)))

(defn print-handler [conn]
  (while (nil? (:exit conn))
    (if-let [msg (.readLine (:in conn))]
      (println "received raw message:" msg))))

(defn connect [server handler]
  (let [sock (socket server)
        in (in sock)
        out (out sock)
        conn {:in in :out out}]
    (doto (Thread. #(handler conn)) (.start))
    conn))

(defn write [conn message]
  (println "sending message:" message)
  (doto (:out conn)
    (.println message)
    (.flush)))
