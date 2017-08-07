(defproject punter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.7.1"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/data.priority-map "0.0.7"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot punter.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns punter.core})
