(ns cordula.system
  (:require [com.stuartsierra.component :as component]
            [cordula.http :refer [new-http-server]]
            [cordula.repository :refer [new-request-repository]]))

(defn new-system
  [conf]
  (component/system-map
   :conf conf
   :request-repository (new-request-repository)
   :http (component/using (new-http-server)
                          [:conf :request-repository])))
