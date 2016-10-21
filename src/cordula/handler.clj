(ns cordula.handler
  (:require [clojure.tools.logging :as log]
            [compojure.api.exception :as ex]
            [compojure.api.routes :as routes]
            [compojure.api.sweet :refer :all]
            [com.stuartsierra.component :as component]
            [cordula.proxy :as proxy]
            [cordula.repository :refer :all]
            [cordula.schema :refer :all]
            [ring.util.http-response :refer :all]
            [ring.util.http-status :as http-status]
            [schema.core :as s]))

(defprotocol DynamicHandler
  (reset [this]))

(defn dynamic-route
  [{:keys [in out id name description] :as request}]
  (let [url (str "/" id (:path in))]
    (log/debugf "Build dynamic route %s (%s)" request url)
    (case (:method in)
      "get" (GET url [] (proxy/proxy-handler request))
      "post" (POST url [] (proxy/proxy-handler request)))))

(defn app
  [requests]
  (log/debug "Build routes: " requests)
  (api
   {:swagger
    {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Cordula"
                    :description "HTTP request adapter"}
             :tags [{:name "api", :description "some apis"}]}}}
    (context "/request/" []
             :components [request-repository handler]
             (resource
              {:tags ["request"]
               :get {:summary "get requests"
                     :description "get all requests"
                     :responses {http-status/ok {:schema [Request]}}
                     :handler (fn [_]
                                (ok (find-all request-repository {})))}
               :post {:summary "add a request"
                      :parameters {:body-params NewRequest}
                      :description "add a new request"
                      :responses {http-status/created {:schema Request}}
                      :handler (fn [{body :body-params}]
                                 (let [{:keys [id] :as request}
                                       (create-request request-repository
                                                       body)]
                                   (reset handler)
                                   (created (path-for ::request {:id id})
                                            request)))}}))
    (context "/request/:id" []
             :path-params [id :- s/Str]
             :components [request-repository handler]
             (resource
              {:tags ["request"]
               :get {:x-name ::request
                     :summary "gets a request"
                     :responses {http-status/ok {:schema Request}}
                     :handler (fn [_]
                                (if-let [request (get-request request-repository
                                                              id)]
                                  (ok request)
                                  (not-found)))}
               :put {:summary "updates a request"
                     :parameters {:body-params UpdatedRequest}
                     :responses {http-status/ok {:schema Request}}
                     :handler (fn [{body :body-params}]
                                (if-let [request
                                         (update-request request-repository
                                                         id
                                                         body)]
                                  (do (reset handler)
                                      (ok request))
                                  (not-found)))}
               :delete {:summary "deletes a request"
                        :handler (fn [_]
                                   (delete-request request-repository id)
                                   (reset handler)
                                   (no-content))}}))
    (->> requests
         (map dynamic-route)
         (apply routes))
    (undocumented (not-found (ok {:not "found"})))))

(defrecord Handler []
  component/Lifecycle
  (start [this]
    (let [app-handler (atom (app (find-all
                                  (:request-repository this)
                                  {})))]
      (assoc this
             :handler-fn
             (fn [request]
               (@app-handler request))
             :app-handler
             app-handler)))
  (stop [this]
    (dissoc this :handler-fn))
  DynamicHandler
  (reset [this]
    (when-let [app-handler (:app-handler this)]
      (reset! app-handler (app (find-all
                                (:request-repository this)
                                {}))))))

(defn new-handler
  []
  (->Handler))
