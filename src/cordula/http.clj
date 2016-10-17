(ns cordula.http
  (:require [clojure.tools.logging :as log]
            [compojure.api.middleware :refer [wrap-components]]
            [com.stuartsierra.component :as component]
            [cordula.handler :refer [app]]
            [org.httpkit.server :as httpkit]))

(defrecord HttpKit []
  component/Lifecycle
  (start [this]
    (let [{:keys [port host]} (:conf this)
          handler (:handler this)]
      (log/infof "Server started at http://%s:%s"
                 host
                 port)
      (assoc this :http-kit (httpkit/run-server
                             (wrap-components
                              (fn [request]
                                ((:handler-fn handler) request))
                              (select-keys this [:request-repository :handler]))
                             {:port port
                              :ip host}))))
  (stop [this]
    (if-let [http-kit (:http-kit this)]
      (http-kit))
    (dissoc this :http-kit)))

(defn new-http-server
  []
  (->HttpKit))
