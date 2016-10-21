(ns cordula.test-helpers
  (:import [java.net ServerSocket])
  (:require  [cheshire.core :as cheshire]
             [compojure.api.middleware :refer [wrap-components]]
             [com.stuartsierra.component :as component]
             [cordula.handler :refer [new-handler]]
             [cordula.repository :refer [new-request-repository]]
             [cordula.system :refer [new-system]]
             [org.httpkit.server :refer [run-server]]
             [reloaded.repl :refer [go set-init! stop]]
             [ring.middleware.defaults :as defaults]
             [ring.mock.request :as mock]))

(def ^:dynamic *dest-port* 3001)

(defn available-port [from]
  (loop [port from]
    (if (try (with-open [sock (ServerSocket. port)]
               (.getLocalPort sock))
             (catch Exception e nil))
      port
      (recur (+ port 1)))))

(defn start-proxyfied-server
  [port]
  (run-server
   (defaults/wrap-defaults
    (fn [handler]
      (fn [request]
        {:status 200
         :body {:ok "hello"}}))
    defaults/site-defaults)
   {:port port}))

(defn fixture-proxyfied-server
  [f]
  (let [proxyfied-server-port (available-port 3000)
        proxyfied-server (start-proxyfied-server
                          proxyfied-server-port)]
    (try
      (binding [*dest-port* proxyfied-server-port]
        (f))
      (finally
        (proxyfied-server)))))

(defn test-system
  []
  (component/system-map
   :request-repository (new-request-repository)
   :handler (component/using (new-handler)
                             [:request-repository])))

(defmacro with-test-handler
  "Evaluates body within a test system. Bounds the application handler to the
  first parameter."
  [handler & body]
  (let [system (gensym 'system)]
    `(let [~system (component/start (test-system))
           ~handler (wrap-components
                       (get-in ~system [:handler :handler-fn])
                       (select-keys ~system [:request-repository :handler]))]
       ~@body
       (component/stop ~system))))

(defn parse-body [body]
  (when (and body
             (not (= body "")))
    (cheshire/parse-string (slurp body) true)))

(defn mock-request
  ([handler method path]
   (mock-request handler method path {}))
  ([handler method path b]
   (let [request (if (#{:get :head :delete} method)
                   (mock/request method path)
                   (-> (mock/request method path)
                       (mock/content-type "application/json")
                       (mock/body (cheshire/generate-string b))))
         {:keys [body error] :as response} (handler request)]
     (assoc response :body (parse-body body)))))
