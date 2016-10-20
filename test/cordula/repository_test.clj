(ns cordula.repository-test
  (:require [cordula.repository :as sut]
            [clojure.test :as t :refer [deftest is testing]]))

(deftest satisfy-properties-test
  (is (sut/satisfy-properties? {:a :a
                                :b :b}
                               {}))
  (is (sut/satisfy-properties? {:a :a
                                :b :b}
                               {:a :a
                                :b :b}))
  (is (sut/satisfy-properties? {:a :a
                                :b :b}
                               {:a :a}))
  (is (not (sut/satisfy-properties? {:a :a
                                     :b :b}
                                    {:a :a
                                     :c :c}))))
