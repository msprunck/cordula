(ns cordula.version
  (:require [leiningen.core.project :refer [read-raw]]))

(defmacro get-version
  "We use a macro to be evaluated at compilation time when
  project.clj is readable."
  []
  (let [project (read-raw "project.clj")]
    {:version (:version project)
     :build (:build-number project)}))
