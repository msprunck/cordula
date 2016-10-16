(ns cordula.http
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [cordula.handler :refer [app]]
            [org.httpkit.server :as httpkit]))

(defrecord HttpKit []
  component/Lifecycle
  (start [this]
    (let [{:keys [port host]} (:conf this)]
      (log/infof "Server started at http://%s:%s"
                 host
                 port)
      (assoc this :http-kit (httpkit/run-server
                             #'app
                             {:port port
                              :ip host}))))
  (stop [this]
    (if-let [http-kit (:http-kit this)]
      (http-kit))
    (dissoc this :http-kit)))

(defn new-http-server
  []
  (->HttpKit))
