(ns cordula.repository
  (:import java.util.UUID)
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defn uuid [] (str (UUID/randomUUID)))

(defprotocol RequestRepository
  (get-request [this id])
  (create-request [this new-query])
  (update-request [this id query])
  (delete-request [this id])
  (find-all [this properties]))

(defn satisfy-properties?
  [query properties]
  (every? true?
          (map (fn [[k v]]
                (= (get query k) v))
               properties)))

(defrecord AtomRequestRepository
    []
  component/Lifecycle
  (start [this]
    (assoc this :db (atom {})))
  (stop [this]
    (dissoc this :db))
  RequestRepository
  (get-request [this id]
    (get @(:db this) id))
  (create-request [this new-request]
    (let [id (uuid)
          r (assoc new-request :id id)]
      (swap! (:db this) assoc id r)
      (get-request this id)))
  (update-request [this id request]
    (let [r (assoc request :id id)]
      (swap! (:db this) update id (constantly r))
      (get-request this id)))
  (delete-request [this id]
    (swap! (:db this) dissoc id))
  (find-all [this properties]
    (filter (fn [x]
              (satisfy-properties? x properties))
            (vals @(:db this)))))

(defn new-request-repository
  []
  (->AtomRequestRepository))
