(ns cordula.components.http
  (:require [clojure.tools.logging :as log]
            [compojure.api.middleware :refer [wrap-components]]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))

(defrecord HttpKit []
  component/Lifecycle
  (start [this]
    (let [{:keys [server-port server-host]} (:conf this)
          {:keys [handler-fn]} (:handler this)]
      (log/infof "Server started at http://%s:%s"
                 server-host
                 server-port)
      (assoc this :http-kit (httpkit/run-server
                             (wrap-components
                              handler-fn
                              (select-keys this [:db :handler]))
                             {:port server-port
                              :ip server-host}))))
  (stop [this]
    (if-let [http-kit (:http-kit this)]
      (http-kit))
    (dissoc this :http-kit)))

(defn new-http-server
  []
  (->HttpKit))
