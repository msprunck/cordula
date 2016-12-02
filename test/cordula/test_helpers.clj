(ns cordula.test-helpers
  (:import [java.net ServerSocket])
  (:require  [clojure.tools.logging :as log]
             [buddy.core.codecs :as codecs]
             [buddy.core.codecs.base64 :as b64]
             [buddy.sign.jwt :as jwt]
             [cheshire.core :as cheshire]
             [clj-time.core :as t]
             [clj-time.coerce :as c]
             [compojure.api.middleware :refer [wrap-components wrap-exceptions
                                               api-middleware-defaults]]
             [com.stuartsierra.component :as component]
             [cordula.components.conf :refer [new-configuration]]
             [cordula.components.mongo :refer [new-mongo-db]]
             [cordula.components.handler :refer [new-handler]]
             [cordula.system :refer [new-system]]
             [org.httpkit.server :refer [run-server]]
             [reloaded.repl :refer [go set-init! stop]]
             [ring.middleware.defaults :as defaults]
             [ring.mock.request :as mock]
             [clojure.string :as string]))

(def ^:dynamic *dest-port* 3001)

;; ----- JWT -----
(def secret "secret")
(def valid-token
  (let [now (-> (t/now)
                c/to-long
                (/ 1000)
                Math/floor)]
    (jwt/sign {:iss "https://msprunck.auth0.com/"
               :sub "google-oauth2|103372409097283586192"
               :aud "8FJx79EfgvltgO0T0PFqjO6X9khPmGWj"
               :exp (+ now 3600)
               :iat now}
              secret
              {:alg :hs256})))
(defn with-valid-token
  "Add an Authorization header with a valid token."
  [request]
  (update-in request
             [:headers]
             #(assoc %
                     :authorization
                     (format "Bearer %s" valid-token))))

(defn available-port [from]
  (loop [port from]
    (if (try (with-open [sock (ServerSocket. port)]
               (.getLocalPort sock))
             (catch Exception e nil))
      port
      (recur (+ port 1)))))

(defn echo-request-handler
  [request]
  (log/debug "Proxy server request" request)
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (cheshire/generate-string
          (merge (select-keys request
                              [:headers
                               :query-params
                               :form-params
                               :uri])
                 (when-let [body (:body request)]
                   {:body (slurp body)})))})

(defn start-proxyfied-server
  [port]
  (log/infof "Proxy server started at http://0.0.0.0:%s" port)
  (run-server
   (-> echo-request-handler
       (defaults/wrap-defaults
        defaults/api-defaults)
       (wrap-exceptions
        (:exceptions api-middleware-defaults)))
   {:port port}))

(defn fixture-proxyfied-server
  [f]
  (let [proxyfied-server-port (available-port 3001)
        proxyfied-server (start-proxyfied-server
                          proxyfied-server-port)]
    (try
      (binding [*dest-port* proxyfied-server-port]
        (f))
      (finally
        (proxyfied-server)))))

(defn proxy-base-url
  []
  (str "http://localhost:" *dest-port*))

(defn test-system
  []
  (component/system-map
   ;; :db-uri can be overriden by DB_URI env variable
   :conf (new-configuration {:db-uri "mongodb://admin:crdl@192.168.99.100:27017/cordula"
                             :client-secret (codecs/bytes->str (b64/encode secret))})
   :db (component/using
        (new-mongo-db)
        [:conf])
   :handler (component/using (new-handler)
                             [:db :conf])))

(defmacro with-test-handler
  "Evaluates body within a test system. Bounds the application handler to the
  first parameter."
  [handler & body]
  (let [system (gensym 'system)]
    `(let [~system (component/start (test-system))
           ~handler (wrap-components
                       (get-in ~system [:handler :handler-fn])
                       (select-keys ~system [:db :handler]))]
       ~@body
       (component/stop ~system))))

(defn parse-body [body]
  (when (and body
             (not (= body "")))
    (let [body-str (slurp body)]
      (log/debug "Parse body:" body-str)
      (cheshire/parse-string body-str true))))

(defn mock-body
  "Add a body to the mock request if not nil"
  [request body]
  (if body
    (-> request
        (mock/content-type "application/json")
        (mock/body (cheshire/generate-string body)))
    request))

(defn mock-headers
  [request headers]
  (let [normalized
        (into {}
              (map (fn [[k v]]
                     [(string/lower-case (name k)) (str v)])
                   headers))]
    (update request
            :headers
            #(into % normalized))))

(defn mock-request
  "Create a minimal valid request map."
  [{:keys [method path body params headers]}]
  (let [request (if (#{:get :head :delete} method)
                  (mock/request method path)
                  (-> (mock/request method path params)
                      (mock-body body)))]
    (mock-headers request headers)))

(defn http-request
  "Create a valid request map and apply it to the handler function.
  Return the response from the handler function."
  ([handler {:keys [body method path params headers auth-token]
             :as props
             :or {auth-token false}}]
   (let [request (mock-request (if auth-token
                                 (with-valid-token props)
                                 props))
         {:keys [body error] :as response} (handler request)
         parsed-resp (assoc response :body (parse-body body))]
     (log/debugf "Mocked request: %s" request)
     (log/debugf "Mocked response: %s" parsed-resp)
     parsed-resp)))
