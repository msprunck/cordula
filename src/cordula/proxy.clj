(ns cordula.proxy
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [ring.util.http-response :refer :all]))

(defn proxy-handler
  [url method]
  (fn [req]
    (log/debugf "Proxying request to %s to remote url %s" (:uri req) url)
    (select-keys (client/request
                  {:url url
                   :method method
                   ;;:body (not-empty (slurp (:body req)))
                   :headers (dissoc (:headers req) "host" "content-length")
                   :throw-exceptions false
                   :as :stream
                   :insecure? true
                   :force-redirects false
                   :follow-redirects false
                   :decompress-body false
                   :socket-timeout 1000
                   :conn-timeout 1000})
                 [:status :headers :body])))
