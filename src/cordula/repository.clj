(ns cordula.repository
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [cordula.components.mongo :as m]
            [monger.collection :as mc]
            [clojure.set :as set]))

(defprotocol RequestRepository
  (init [this])
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

(def collection "requests")

(extend-protocol RequestRepository
  cordula.components.mongo.MongoDB
  (init [this]
    (m/ensure-collection (:mdb this) collection))
  (get-request [this id]
    (set/rename-keys (mc/find-map-by-id (:mdb this) collection id)
                     {:_id :id}))
  (create-request [this new-request]
    (set/rename-keys
     (mc/insert-and-return (:mdb this)
                           collection
                           (set/rename-keys new-request {:id :_id}))
     {:_id :id}))
  (update-request [this id request]
    (let [{:keys [created_at]} (get-request this id)]
      (mc/update-by-id (:mdb this)
                       collection
                       id
                       (assoc request :created_at created_at)))
    (get-request this id))
  (delete-request [this id]
    (mc/remove-by-id (:mdb this) collection id))
  (find-all [this properties]
    (map
     #(set/rename-keys % {:_id :id})
     (mc/find-maps (:mdb this) collection properties)))

  clojure.lang.Atom
  (get-request [this id]
    (get @this id))
  (create-request [this new-request]
    (swap! this assoc (:id new-request) new-request)
    (get-request this (:id new-request)))
  (update-request [this id request]
    (let [{:keys [created_at]} (get-request this id)
          r (assoc request :id id :created_at created_at)]
      (swap! this update id (constantly r))
      (get-request this id)))
  (delete-request [this id]
    (swap! this dissoc id))
  (find-all [this properties]
    (filter (fn [x]
              (satisfy-properties? x properties))
            (vals @this))))
