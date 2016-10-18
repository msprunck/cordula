(ns cordula.proxy
  (:require [clojure.tools.logging :as log]
            [org.httpkit.client :as http]
            [ring.util.http-response :refer :all]))

(defn proxy-handler
  [path method]
  (fn [request]
    (log/debug "proxied request" request)
    (ok {})))
