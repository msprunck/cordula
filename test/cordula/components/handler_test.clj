(ns cordula.components.handler-test
  (:require [cordula.components.handler :as sut]
            [cordula.test-helpers :as th]
            [clojure.test :as t :refer [deftest is testing
                                        use-fixtures]]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(deftest crud-test
  (th/with-test-handler app
    (testing "Request CRUD operations"
      (let [conf {:name "request1"
                  :in {:path "/api/:param1/"
                       :method "get"}
                  :proxy {:uri "https://api.com/test/"
                          :method "post"}}
            {:keys [body]} (th/http-request app
                                            {:method :post
                                             :path "/request/"
                                             :body conf
                                             :auth-token true})
            req-id (:id body)
            req-created-at (:created_at body)
            req-path (str "/request/" req-id)]
        (is (= conf
               (dissoc body :id :created_at :updated_at :owner))
            "POST a new request configuration")
        (is (= body
               (-> (th/http-request app
                                    {:method :get
                                     :path "/request/"
                                     :auth-token true})
                   :body
                   last))
            "GET all requests")
        (is (= 401
               (:status (th/http-request app
                                    {:method :get
                                     :path "/request/"
                                     :auth-token false})))
            "Not authorized")
        (is (= body
               (:body
                (th/http-request app
                                 {:method :get
                                  :path req-path
                                  :body {:_id req-id}
                                  :auth-token true})))
            "GET the created request")
        (let [updated-conf (assoc body :name "request-modified")
              {:keys [body status]} (th/http-request app
                                              {:method :put
                                               :path req-path
                                               :body (dissoc updated-conf
                                                             :owner
                                                             :id
                                                             :updated_at
                                                             :created_at)
                                               :auth-token true})]
          (is (= 200 status) "Request updated")
          (is (= (dissoc updated-conf :updated_at)
                 (dissoc
                  (:body (th/http-request app
                                          {:method :get
                                           :path req-path
                                           :auth-token true}))
                  :updated_at))
              "UPDATE request configuration"))
        (th/http-request app
                         {:method :delete
                          :path req-path
                          :auth-token true})
        (is (= 404
               (:status (th/http-request app
                                         {:method :get
                                          :path req-path
                                          :auth-token true})))
            "DELETE request configuration")))))
