(ns cordula.components.repository
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [cordula.components.mongo :as m]
            [monger.collection :as mc]
            [clojure.set :as set]))

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
    (swap! (:db this) assoc (:id new-request) new-request)
    (get-request this (:id new-request)))
  (update-request [this id request]
    (let [{:keys [created_at]} (get-request this id)
          r (assoc request :id id :created_at created_at)]
      (swap! (:db this) update id (constantly r))
      (get-request this id)))
  (delete-request [this id]
    (swap! (:db this) dissoc id))
  (find-all [this properties]
    (filter (fn [x]
              (satisfy-properties? x properties))
            (vals @(:db this)))))

(def collection "requests")

(defrecord MongoDBRequestRepository
    []
  component/Lifecycle
  (start [this]
    (let [db (get-in this [:db :db])]
      (m/ensure-collection db collection)
      (assoc this :db db)))
  (stop [this]
    (dissoc this :db))
  RequestRepository
  (get-request [this id]
    (set/rename-keys (mc/find-map-by-id (:db this) collection id)
                     {:_id :id}))
  (create-request [this new-request]
    (set/rename-keys
     (mc/insert-and-return (:db this)
                           collection
                           (set/rename-keys new-request {:id :_id}))
     {:_id :id}))
  (update-request [this id request]
    (let [{:keys [created_at]} (get-request this id)]
      (mc/update-by-id (:db this)
                       collection
                       id
                       (assoc request :created_at created_at)))
    (get-request this id))
  (delete-request [this id]
    (mc/remove-by-id (:db this) collection id))
  (find-all [this properties]
    (map
     #(set/rename-keys % {:_id :id})
     (mc/find-maps (:db this) collection properties))))

(defn new-request-repository
  []
  (->MongoDBRequestRepository))
