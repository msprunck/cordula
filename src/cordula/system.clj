(ns cordula.system
  (:require [com.stuartsierra.component :as component]
            [cordula.http :refer [new-http-server]]))

(defn new-system
  [conf]
  (component/system-map
   :conf conf
   :http (component/using (new-http-server)
                          [:conf])))
