(ns cordula.components.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.exception :as ex]
            [compojure.api.routes :as routes]
            [compojure.api.sweet :refer :all]
            [com.stuartsierra.component :as component]
            [cordula.repository :as r]
            [cordula.lib.dynamic-handler :refer :all]
            [cordula.middlewares.auth :refer [wrap-authentication
                                              authenticated-mw]]
            [cordula.routes.dynamic :refer [dynamic-routes]]
            [cordula.routes.request :refer [request-routes]]
            [cordula.schema :refer :all]
            [cordula.version :refer [get-version]]
            [ring.util.http-response :refer :all]
            [ring.util.http-status :as http-status]
            [schema.core :as s]))

(defn app
  [db conf]
  (let [request-confs (r/find-all db {})
        {:keys [client-secret client-id scope token-name
                authorization-url]} conf
        authentication-mw
        #(wrap-authentication % client-secret
                              (fn [request e]
                                (log/debug e "Authentication error")))]
    (log/debug "Build routes: " request-confs)
    (api
     {:exceptions {:handlers {::ex/request-parsing
                              (ex/with-logging
                                ex/request-parsing-handler :error)
                              ::ex/request-validation
                              (ex/with-logging
                                ex/request-validation-handler :error)
                              ::ex/response-validation
                              (ex/with-logging
                                ex/response-validation-handler :error)}}
      :swagger
      {:ui "/"
       :spec "/swagger.json"
       :data {:info {:title "Cordula"
                     :description "HTTP request adapter"}
              :tags [{:name "api", :description "some apis"}]
              :securityDefinitions
              {:oauth2 {:type "oauth2"
                        :scopes {scope scope}
                        :authorizationUrl authorization-url
                        :flow "implicit"
                        :tokenName token-name}}}
       :options {:ui {:oauth2 {:clientId client-id
                               :appName "blank"
                               :realm "blank"}}}}}
     (context "/version" []
              :tags ["version"]
              (GET "/" []
                   :return Version
                   :summary "API version details"
                   (ok (get-version))))
     (context "" []
              :middleware [authentication-mw authenticated-mw]
              request-routes
              (dynamic-routes request-confs))
     (undocumented (not-found (ok {:not "found"}))))))

(defrecord Handler []
  component/Lifecycle
  (start [this]
    (r/init (:db this))
    (let [app-atom (atom (app (:db this) (:conf this)))]
      (assoc this
             :handler-fn
             (fn [request]
               (@app-atom request))
             :app
             app-atom)))
  (stop [this]
    (dissoc this :handler-fn))
  DynamicHandler
  (reset [this]
    (when-let [app-atom (:app this)]
      (reset! app-atom (app (:db this) (:conf this))))))

(defn new-handler
  []
  (->Handler))
