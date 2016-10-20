(ns cordula.server
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [cordula.system :refer [new-system]]
            [reloaded.repl :refer [go set-init! stop]]))

;; Uncaught exceptions handling
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/fatalf ex "Uncaught exception on %s" (.getName thread))
     (System/exit -1))))

(def description
  "Application description"
  "Cordula - HTTP request adapter")

(def cli-options
  "Command line options"
  [[nil "--help" "Show help"]
   [nil "--host HOST"
    :default "0.0.0.0"]
   [nil "--port PORT"
    :default 8080
    :parse-fn #(Integer/parseInt %)]])

(defn parse-args
  "Parses command line arguments and display help or errors if needed"
  [args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (when errors
      (binding  [*out* *err*]
        (println (clojure.string/join "\n" errors))
        (println description)
        (println summary)
        (System/exit 0)))
    (when (:help options)
      (println description)
      (println summary)
      (System/exit 0))
    options))

(defn -main [& args]
  (set-init! #(new-system (parse-args args)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop))
  (go))
