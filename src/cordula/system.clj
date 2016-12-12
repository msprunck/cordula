(ns cordula.system
  (:require [com.stuartsierra.component :as component]
            [cordula.components.conf :refer [new-configuration]]
            [cordula.components.mongo :refer [new-mongo-db]]
            [cordula.components.handler :refer [new-handler]]
            [cordula.components.http :refer [new-http-server]]))

(defn new-system
  [args]
  (component/system-map
   :conf (new-configuration args)
   :db (component/using
        (new-mongo-db)
        [:conf])
   :handler (component/using (new-handler)
                             [:db :conf])
   :http (component/using (new-http-server)
                          [:conf :db :handler])))
