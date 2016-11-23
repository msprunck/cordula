(ns cordula.lib.helpers
  (:import java.util.UUID))

(defn uuid [] (str (UUID/randomUUID)))
(defn now [] (java.util.Date.))
