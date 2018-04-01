(ns mim
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]))

(def trampoline-file (fs/expand-home "~/.mim/TRAMPOLINE"))

(defn task [cmd]
  ;; We assume that trampolining is on by default
  (spit trampoline-file cmd)
  "Trampolining task...")
