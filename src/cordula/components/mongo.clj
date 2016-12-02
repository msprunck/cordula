(ns cordula.components.mongo
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.credentials :as mcred]
            [clojure.string :as str]))

(defn ensure-collection
  "Creates a collection if it does not exist"
  [db collection]
  (when-not (mc/exists? db collection)
    (mc/create db collection {})))

(defn star-password
  "Replaces the password in the given uri by stars."
  [uri-str]
  (let [uri (java.net.URI. uri-str)
        user-info (.getUserInfo uri)
        [user password] (when user-info (str/split user-info #":"))]
    (if password
      (str/replace uri-str password "*****")
      uri-str)))

(defrecord MongoDB
    []
  component/Lifecycle
  (start [this]
    (let [{:keys [db-uri]} (:conf this)
          {:keys [db conn]} (mg/connect-via-uri db-uri)]
      (log/infof "Connection to MongoDB (%s)"
                 (star-password db-uri))
      (assoc this :conn conn :mdb db)))
  (stop [this]
    (try
      (when-let [conn (:conn this)]
        (mg/disconnect conn))
      (catch Throwable t (log/error t "Error when stopping MongoDB component"))
      (finally (dissoc this :conn :mdb)))))

(defn new-mongo-db
  []
  (->MongoDB))
