(ns cordula.handler-test
  (:require [cordula.handler :as sut]
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
            {:keys [body]} (th/mock-request app :post "/request/" conf)
            req-id (:id body)
            req-path (str "/request/" req-id)]
        (is (= conf
               (dissoc body :id))
            "POST a new request configuration")
        (is (= body
               (-> (th/mock-request app :get "/request/")
                   :body
                   first))
            "GET all requests")
        (is (= body
               (:body
                (th/mock-request app :get req-path)))
            "GET the created request")
        (let [updated-conf (assoc conf :name "request-modified")
              {:keys [body]} (th/mock-request app
                                              :put
                                              req-path
                                              updated-conf)]
          (is (= (assoc updated-conf :id req-id)
                 (:body (th/mock-request app :get req-path)))
              "UPDATE request configuration"))
        (th/mock-request app :delete req-path)
        (is (= 404
               (:status (th/mock-request app :get req-path))))))))
