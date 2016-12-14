(ns cordula.middlewares.auth
  (:require [clojure.tools.logging :as log]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.middleware :as bm]
            [buddy.core.codecs.base64 :as b64]
            [compojure.api.meta :refer [restructure-param]]
            [cordula.schema :as cs]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(defn wrap-authentication
  [handler secret on-error]
  (let [auth-backend (jws-backend {:secret (b64/decode secret)
                                   :options {:alg :hs256}
                                   :token-name "Bearer"
                                   :on-error on-error})]
    (bm/wrap-authentication handler auth-backend)))

(defn authenticated-mw
  "Middleware used in routes that require authentication. If request is not
   authenticated a 401 not authorized response will be returned"
  [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      (unauthorized {:error "Not authorized"}))))

(s/defn ->user :- (s/maybe cs/User)
  "Get the current user from an HTTP request"
  [request]
  (when-let [{:keys [sub] :as identity} (:identity request)]
    {:id sub
     :identities [{:provider "oauth2"
                   :user_id sub
                   :connection "oauth2"
                   :isSocial false}]}))

(defmethod #^{:doc "Provide the current user in the routes meta data"}
  restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(->user ~'+compojure-api-request+)]))

(defn can-access?
  [res user]
  (let [userid (:id user)
        owner (:owner res)
        result (and userid
                    owner
                    (= userid owner))]
    (log/debugf "user %s can access %s ? : %s" user res result)
    result))
