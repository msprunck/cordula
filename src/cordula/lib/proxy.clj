(ns cordula.lib.proxy
  (:require [clojure.algo.generic.functor :refer [fmap]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.walk :refer [stringify-keys]]
            [clj-http.client :as client]
            [cordula.schema :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn with-default-options
  [p]
  (into p
        {:throw-exceptions false
         :as :stream
         :insecure? true
         :force-redirects false
         :follow-redirects false
         :decompress-body false
         :socket-timeout 1000
         :conn-timeout 1000
         :debug true}))

(defn- format-param->value
  [[_ param-str] values]
  (let [path (string/split param-str #"\/")]
    (get-in values path "")))

(defn format-str
  "Replace variables with their value. Variables are tags enclosed by curly
  brackets prefixed by a tild and will be replaced with the respective data.

  Ex:
  Template:
  \"Hello ~{params/param1}!\"

  Data:
  {\"params\" {\"param1\" \"Matthieu\"}}

  Result:
  \"Hello Matthieu!\"
  "
  [s values]
  (when s
    (string/replace s #"~\{([^~\{\}]*)\}" #(format-param->value % values))))

(s/defn with-headers
  "Merge request headers with those configured."
  [params req conf :- Request apply-template]
  (let [req-headers (dissoc (:headers req) "host" "content-length")
        conf-headers (fmap apply-template
                           (get-in conf [:proxy :headers] {}))]
    (log/debug "Req headers:" (:headers req) " Conf headers:" conf-headers)
    (into params {:headers (into req-headers conf-headers)})))

(s/defn with-body
  "Replace the request body by the configured one."
  [params req conf :- Request apply-template]
  (let [conf-body (apply-template
                   (get-in conf [:proxy :body]))
        req-body (:body req)
        body (or conf-body req-body)]
    (log/debug "Configured request body:" conf-body)
    (if body
      (into params {:body body})
      params)))

(s/defn with-params
  "Replace or merge form and query params."
  [params req conf :- Request apply-template]
  (apply merge
         params
         (map (fn [k]
                (if-let [{:keys [merge-values values]} (get-in conf [:proxy k])]
                  (let [formatted-values (fmap apply-template values)]
                    {k (if merge-values
                         (into (get req k) formatted-values)
                         formatted-values)})
                  {}))
              [:form-params :query-params])))

(defn- extract-vars
  [conf req]
  (stringify-keys
   {:request {:headers (:headers req)
              :body (:body-params req)
              :params (:params req)}}))

(s/defn http-options
  "Build HTTP options from a request and a request configuration."
  [req conf :- Request]
  (let [{:keys [uri method]} (:proxy conf)
        vars (extract-vars conf req)
        apply-template #(format-str % vars)]
    (-> {:url (apply-template uri)
         :method (keyword method)}
        with-default-options
        (with-headers req conf apply-template)
        (with-body req conf apply-template)
        (with-params req conf apply-template))))

(s/defn proxy-handler
  "Build a handler based on the given request configuration."
  [conf :- Request]
  (fn [req]
    (log/debugf "Proxying request to %s to remote url %s"
                (:uri req)
                (get-in conf [:proxy :uri]))
    (log/debug "HTTP options" (http-options req conf) req)
    (select-keys (client/request
                  (http-options req conf))
                 [:status :headers :body])))
