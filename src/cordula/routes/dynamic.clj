(ns cordula.routes.dynamic
  (:require [clojure.tools.logging :as log]
            [compojure.api.sweet :refer :all]
            [cordula.lib.proxy :as proxy]
            [cordula.middlewares.auth :refer [authenticated-mw]]
            [schema.core :as s]))

(defn dynamic-route
  [{:keys [in out id name description] :as request}]
  (let [url (str "/" id (:path in))]
    (log/debugf "Build dynamic route %s (%s)" request url)
    (case (:method in)
      "get" (GET url []
                 :middleware [authenticated-mw]
                 (proxy/proxy-handler request))
      "post" (POST url []
                   :middleware [authenticated-mw]
                   :body [body s/Any]
                   (proxy/proxy-handler request)))))

(defn dynamic-routes
  "Build dynamic routes based on a collection of request configuration."
  [requests]
  (->> requests
       (map dynamic-route)
       (apply routes)))
