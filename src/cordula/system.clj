(ns cordula.system
  (:require [com.stuartsierra.component :as component]
            [cordula.components.conf :refer [new-configuration]]
            [cordula.components.mongo :refer [new-mongo-db]]
            [cordula.components.handler :refer [new-handler]]
            [cordula.components.http :refer [new-http-server]]
            [cordula.components.repository :refer [new-request-repository]]))

(defn new-system
  [args]
  (component/system-map
   :conf (new-configuration args)
   :db (component/using
        (new-mongo-db)
        [:conf])
   :request-repository (component/using
                        (new-request-repository)
                        [:conf :db])
   :handler (component/using (new-handler)
                             [:request-repository])
   :http (component/using (new-http-server)
                          [:conf :request-repository :handler])))
