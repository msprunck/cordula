(ns cordula.proxy-test
  (:require [cordula.proxy :as sut]
            [clojure.test :as t :refer [deftest is testing
                                        use-fixtures]]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(deftest format-str-test
  (is (= (sut/format-str "https://www.example.com?a=~{a}&c=~{b/c}"
                         {"a" "a"
                          "b" {"c" "c"}})
         "https://www.example.com?a=a&c=c")))

