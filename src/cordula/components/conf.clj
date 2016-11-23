(ns cordula.components.conf
  (:require [com.stuartsierra.component :as component]
            [cprop.source :as source]))

(defrecord Configuration
    [args]
  component/Lifecycle
  (start [this]
    (merge this
           args
           (source/from-system-props)
           (source/from-env)))
  (stop [this]
    this))

(defn new-configuration
  [args]
  (->Configuration args))

