(ns cordula.handler
  (:require [compojure.api.sweet :refer :all]
            [cordula.repository :refer :all]
            [ring.util.http-response :refer :all]
            [ring.util.http-status :as http-status]
            [schema.core :as s]))

(s/defschema Request
  {:id s/Str
   :name s/Str
   (s/optional-key :description) s/Str})

(s/defschema NewRequest (dissoc Request :id))
(s/defschema UpdatedRequest NewRequest)

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Cordula"
                    :description "HTTP request adapter"}
             :tags [{:name "api", :description "some apis"}]}}}
    (context "/request/" []
             :components [request-repository]
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
                                   (created (path-for ::request {:id id}) request)))}}))
    (context "/request/:id" []
             :path-params [id :- s/Str]
             :components [request-repository]

             (resource
              {:tags ["request"]
               :get {:x-name ::request
                     :summary "gets a request"
                     :responses {http-status/ok {:schema Request}}
                     :handler (fn [_]
                                (if-let [request (get-request request-repository id)]
                                  (ok request)
                                  (not-found)))}
               :put {:summary "updates a request"
                     :parameters {:body-params UpdatedRequest}
                     :responses {http-status/ok {:schema Request}}
                     :handler (fn [{body :body-params}]
                                (if-let [request (update-request request-repository
                                                                 id
                                                                 body)]
                                  (ok request)
                                  (not-found)))}
               :delete {:summary "deletes a request"
                        :handler (fn [_]
                                   (delete-request request-repository id)
                                   (no-content))}}))))
