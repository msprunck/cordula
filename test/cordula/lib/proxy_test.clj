(ns cordula.lib.proxy-test
  (:require [cheshire.core :as cheshire]
            [cordula.lib.proxy :as sut]
            [cordula.test-helpers :as th]
            [clojure.test :as t :refer [deftest is testing
                                        use-fixtures]]
            [schema.test :as st]))


(use-fixtures :once st/validate-schemas th/fixture-proxyfied-server)

(deftest format-str-test
  (is (= (sut/format-str "https://www.example.com?a=~{a}&c=~{b/c}"
                         {"a" "a"
                          "b" {"c" "c"}})
         "https://www.example.com?a=a&c=c"))
  (is (= (sut/format-str (cheshire/generate-string
                          {:data "~{b/c}"})
                         {"a" "a"
                          "b" {"c" "c"}})
         "{\"data\":\"c\"}")))

(defn- request
  "Creates a request configuration, calls the generated route and return
  the response.The configuration is deleted before the response is returned.
  The body of the response should contain the request sent to the proxy."
  ([app conf {:keys [path method params body headers]
              :or {method (keyword (get-in conf [:in :method] "get"))}}]
   (let [create-resp (th/http-request app {:method :post
                                           :body conf
                                           :path "/request/"})
         req-id (get-in create-resp [:body :id])
         req-path (str "/" req-id path)
         response (th/http-request app {:method method
                                        :path req-path
                                        :headers headers
                                        :params params
                                        :body body})]
     (th/http-request app {:method :delete
                           :path (str "/request/" req-id)})
     response)))

(deftest proxy-handler-data-extraction-test
  (th/with-test-handler app
    (testing "Data extraction"
      (testing "Path and query parameters"
        (let [conf {:name "request1"
                    :in {:path "/api/:param1/:param2"
                         :method "get"}
                    :proxy
                    {:uri
                     (str (th/proxy-base-url)
                          "?param1=~{request/params/param1}&param2=~{request/params/param2}"
                          "&param3=~{request/params/param3}&param4=~{request/params/param4}")
                     :method "get"}}]
          (is
           (= (-> (request app conf
                           {:path
                            "/api/value1/value2?param3=value3&param4=value4"})
                  (get-in [:body :query-params]))
              {:param1 "value1"
               :param2 "value2"
               :param3 "value3"
               :param4 "value4"}))))
      (testing "Form params"
        (let [conf {:name "request2"
                    :in {:path "/api/"
                         :method "post"}
                    :proxy
                    {:uri
                     (str (th/proxy-base-url)
                          "?param1=~{request/params/param1}&param2=~{request/params/param2}")
                     :method "get"}}]
          (is (= (-> (request app conf
                              {:path "/api/"
                               :params {:param1 "value1"
                                        :param2 "value2"}})
                     (get-in [:body :query-params]))
                 {:param1 "value1"
                  :param2 "value2"}))))
      (testing "Request Body"
        (let [conf {:name "request3"
                    :in {:path "/api/"
                         :method "post"}
                    :proxy
                    {:uri
                     (str (th/proxy-base-url)
                          "?param1=~{request/body/prop1}&param2=~{request/body/data/prop2}")
                     :method "get"}}]
          (is (= (-> (request app conf
                              {:path "/api/"
                               :body {:prop1 "value1"
                                      :data {:prop2 "value2"}}})
                     (get-in [:body :query-params]))
                 {:param1 "value1"
                  :param2 "value2"}))))
      (testing "Headers"
        (let [conf {:name "request4"
                    :in {:path "/api/"
                         :method "get"}
                    :proxy
                    {:uri
                     (str (th/proxy-base-url)
                          "?param1=~{request/headers/header1}")
                     :method "get"}}]
          (is (= (-> (request app conf
                              {:path "/api/"
                               :headers {"header1" "value1"}})
                     (get-in [:body :query-params]))
                 {:param1 "value1"})))))))

(deftest proxy-handler-variable-substitution-test
  (th/with-test-handler app
    (testing "Variable substitution"
      (testing "URI"
        (let [conf {:name "request4"
                    :in {:path "/api/:param1/"
                         :method "post"}
                    :proxy {:uri
                            (str (th/proxy-base-url)
                                 "?param1=~{request/params/param1}")
                            :method "get"}}]
          (is (= (-> (request app conf
                              {:path "/api/value1/"})
                     (get-in [:body :query-params]))
                 {:param1 "value1"}))))
      (testing "Form params"
        (let [conf {:name "request5"
                    :in {:path "/api/:param1/"
                         :method "post"}
                    :proxy {:uri (th/proxy-base-url)
                            :method "post"
                            :form-params {:values {:param1 "~{request/params/param1}"}
                                          :merge-values false}}}
              conf-with-merge (assoc-in conf
                                        [:proxy :form-params :merge-values]
                                        true)]
          (is (= (-> (request app conf
                              {:path "/api/value1/"
                               :params {:param2 "value2"}})
                     (get-in [:body :form-params]))
                 {:param1 "value1"}))
          (is (= (-> (request app conf-with-merge
                              {:path "/api/value1/"
                               :params {:param2 "value2"}})
                     (get-in [:body :form-params]))
                 {:param1 "value1"
                  :param2 "value2"}))))
      (testing "Query params"
        (let [conf {:name "request6"
                    :in {:path "/api/:param1/"
                         :method "post"}
                    :proxy {:uri (th/proxy-base-url)
                            :method "post"
                            :query-params {:values {:param1 "~{request/params/param1}"}
                                           :merge-values false}}}
              conf-with-merge (assoc-in conf
                                        [:proxy :query-params :merge-values]
                                        true)]
          (is (= (-> (request app conf
                              {:path "/api/value1/?param2=value2"})
                     (get-in [:body :query-params]))
                 {:param1 "value1"}))
          (is (= (-> (request app conf-with-merge
                              {:path "/api/value1/?param2=value2"})
                     (get-in [:body :query-params]))
                 {:param1 "value1"
                  :param2 "value2"}))))
      (testing "Headers"
        (let [conf {:name "request7"
                    :in {:path "/api/:param1/"
                         :method "post"}
                    :proxy {:uri (th/proxy-base-url)
                            :method "post"
                            :headers {"param2" "~{request/params/param1}"}}}]
          (is (= (-> (request app conf
                              {:path "/api/value1/"})
                     (get-in [:body :headers :param2]))
                 "value1"))))
      (testing "Body"
        (let [conf {:name "request8"
                    :in {:path "/api/:param1/"
                         :method "post"}
                    :proxy {:uri (th/proxy-base-url)
                            :method "post"
                            :headers {"content-type" "application/json"}
                            :body (cheshire/generate-string {:data "~{request/params/param1}"})}}]
          (is (= (-> (request app conf
                              {:path "/api/value1/"})
                     (get-in [:body :body]))
                 "{\"data\":\"value1\"}")))))))
