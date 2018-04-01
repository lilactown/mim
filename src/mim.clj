(ns mim
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]))

(def trampoline-file (fs/expand-home "~/.mim/TRAMPOLINE"))

(defn trampoline-task
  ([cmd] (spit trampoline-file cmd))
  ([cmd cwd] (let [new-cmd (str "sh -c 'cd " cwd " && " cmd "'")]
               (println "New command:" new-cmd)
               (spit trampoline-file new-cmd))))

(defn task [cmd & {:keys [cwd trampoline]}]
  ;; We assume that trampolining is on by default
  (if (nil? cwd)
    (trampoline-task cmd)
    (trampoline-task cmd cwd))
  "Trampolining task...")
