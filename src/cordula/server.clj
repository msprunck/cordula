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
  "Usage of Cordula - HTTP request adapter")

(def cli-options
  "Command line options"
  [[nil "--help" "Show help"]
   [nil "--server-host"
    "Which IP to bind"
    :default "0.0.0.0"]
   [nil "--server-port"
    "Which port listens for incoming requests"
    :default 8080
    :parse-fn #(Integer/parseInt %)]
   [nil "--db-uri"
    "MongoDB URI"
    :default "mongodb://admin:crdl@192.168.99.100:27017/cordula"]
   [nil
    "--client-secret"
    "The Client Secret is used to sign the access token"]
   [nil
    "--client-id"
    "The application's client ID (for Swagger auth)"
    :default "0ZE6WlsV37O07xHsBD6dUikKBtw4wvVB"]
   [nil
    "--token-name"
    "The access token parameter name returned by the authorization endpoint (for Swagger auth)"
    :default "id_token"]
   [nil
    "--authorization-url"
    "the API authorization endpoint (for Swagger auth)"
    :default "https://cordula.auth0.com/authorize"]
   [nil
    "--scope"
    "Specifies the level of access that the application is requesting (for Swagger auth)"
    :default "openid"]])

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
  (let [props (parse-args args)]
    (set-init! #(new-system props)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop))
  (go))
