(ns punter.util)

(def offline-mode false)

(defmacro log [& args]
  (dosync 
    ;(binding [*out* *err*]
      `(println ~@args)))
