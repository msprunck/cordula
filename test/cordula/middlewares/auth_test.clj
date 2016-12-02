(ns cordula.middlewares.auth-test
  (:require [cordula.middlewares.auth :as sut]
            [clojure.test :as t :refer [deftest is testing]]))

(deftest can-access-test
  (is (not (sut/can-access? {} {})))
  (is (not (sut/can-access? {:owner "user"} {})))
  (is (not (sut/can-access? {} {:id "user"})))
  (is (sut/can-access? {:owner "user"} {:id "user"}))
  (is (not (sut/can-access? {:owner "user1"} {:id "user"}))))
