(ns mim.core
  (:require [me.raynes.conch.low-level :as sh])
  (:gen-class))

(def tasks {:build "echo hello"})


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
