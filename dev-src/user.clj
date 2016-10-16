(ns user
  (:require [reloaded.repl :refer [set-init! system init start stop go reset]]))

(set-init! #(do (require 'cordula.system)
                ((resolve 'cordula.system/new-system) {:port 8080
                                                       :host "0.0.0.0"})))

