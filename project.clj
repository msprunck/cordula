(defproject cordula "0.1.0-SNAPSHOT"
  :description "Cordula - HTTP request adapter"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [log4j/log4j "1.2.17"]
                 [com.stuartsierra/component "0.3.1"]
                 [metosin/compojure-api "1.1.8"]
                 [clj-http "3.1.0"]
                 [http-kit "2.2.0"]
                 [reloaded.repl "0.2.3"]]
  :main cordula.server
  :profiles {:dev {:repl-options {:init-ns user}
                   :source-paths ["src" "dev-src"]
                   :dependencies [[cheshire "5.6.3"]
                                  [ring/ring-mock "0.3.0"]]}})
