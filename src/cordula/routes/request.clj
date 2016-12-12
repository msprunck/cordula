(ns cordula.routes.request
  (:require [compojure.api.sweet :refer :all]
            [cordula.repository :as r]
            [cordula.lib.dynamic-handler :refer :all]
            [cordula.lib.helpers :as h]
            [cordula.middlewares.auth :refer [can-access?]]
            [cordula.schema :refer :all]
            [ring.util.http-response :refer :all]
            [ring.util.http-status :as http-status]
            [schema.core :as s]
            [clojure.tools.logging :as log]))

(defroutes request-routes
  (context "/request/" []
           :components [db handler]
           :current-user user
           (resource
            {:tags ["request"]
             :get {:summary "get requests"
                   :description "get all requests"
                   :responses {http-status/ok {:schema [Request]}}
                   :handler (fn [_]
                              (ok (r/find-all db {:owner (:id user)})))}
             :post {:summary "add a request"
                    :parameters {:body-params NewRequest}
                    :description "add a new request"
                    :responses {http-status/created {:schema Request}}
                    :handler
                    (fn [{body :body-params}]
                      (let [{:keys [id] :as request}
                            (r/create-request db
                                              (into body
                                                    {:id (h/uuid)
                                                     :owner (:id user)
                                                     :created_at (h/now)
                                                     :updated_at (h/now)}))]
                        (reset handler)
                        (created (path-for ::request {:id id})
                                 request)))}}))
  (context "/request/:id" []
            :path-params [id :- s/Str]
            :components [db handler]
            :current-user user
            (resource
             {:tags ["request"]
              :get {:x-name ::request
                    :summary "gets a request"
                    :responses {http-status/ok {:schema Request}}
                    :handler (fn [_]
                               (if-let [request (r/get-request db id)]
                                 (if (can-access? request user)
                                     (ok request)
                                     (unauthorized {:error "Not authorized"}))
                                 (not-found)))}
              :put {:summary "updates a request"
                    :parameters {:body-params UpdatedRequest}
                    :responses {http-status/ok {:schema Request}}
                    :handler
                    (fn [{body :body-params}]
                      (if (can-access? (r/get-request db id) user)
                        (if-let [request
                                 (r/update-request db
                                                   id
                                                   (into body
                                                         {:updated_at (h/now)
                                                          :owner (:id user)}))]
                          (do (reset handler)
                              (ok request))
                          (not-found))
                        (unauthorized {:error "Not authorized"})))}
              :delete {:summary "deletes a request"
                       :handler
                       (fn [_]
                         (if (can-access? (r/get-request db id) user)
                           (do (r/delete-request db id)
                               (reset handler)
                               (no-content))
                           (unauthorized {:error "Not authorized"})))}})))
