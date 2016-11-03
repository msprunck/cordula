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
            {:keys [body]} (th/http-request app {:method :post
                                                 :path "/request/"
                                                 :body conf})
            req-id (:id body)
            req-path (str "/request/" req-id)]
        (is (= conf
               (dissoc body :id))
            "POST a new request configuration")
        (is (= body
               (-> (th/http-request app {:method :get
                                         :path "/request/"})
                   :body
                   first))
            "GET all requests")
        (is (= body
               (:body
                (th/http-request app {:method :get
                                      :path req-path})))
            "GET the created request")
        (let [updated-conf (assoc conf :name "request-modified")
              {:keys [body]} (th/http-request app {:method :put
                                                   :path req-path
                                                   :body updated-conf})]
          (is (= (assoc updated-conf :id req-id)
                 (:body (th/http-request app {:method :get
                                              :path req-path})))
              "UPDATE request configuration"))
        (th/http-request app {:method :delete
                              :path req-path})
        (is (= 404
               (:status (th/http-request app {:method :get
                                              :path req-path}))))))))
