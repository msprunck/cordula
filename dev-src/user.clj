(ns user
  (:require [reloaded.repl :refer [set-init! system init start stop go reset]]))

(set-init!
 #(do (require 'cordula.system)
      ((resolve 'cordula.system/new-system)
       {:server-port 8080
        :server-host "0.0.0.0"
        :db-uri
        "mongodb://admin:crdl@192.168.99.100:27017/cordula"
        :client-secret
        (System/getenv "CLIENT_SECRET")
        :client-id
        "0ZE6WlsV37O07xHsBD6dUikKBtw4wvVB"
        :authorization-url
        "https://cordula.auth0.com/authorize"})))
