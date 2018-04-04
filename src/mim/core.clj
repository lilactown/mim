(ns mim.core
  (:require [me.raynes.conch.low-level :as sh]
            [mount.core :as mount :refer [defstate]]
            [clojure.core.server :as socket]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [mim.commands :as commands]
            [mim.pid :as pid])
  (:gen-class))


(defn parse-payload
  "Parses a string payload into EDN"
  [payload]
  (log/info "Parsing" payload)
  (let [parsed (clojure.edn/read-string payload)
        ;; get args as a vector
        args (if (:args parsed)
                 (str/split (:args parsed) #" ")
                 [])]
    (assoc parsed :args args)))


(defn server-handler
  "Handles an incoming socket request.
   Reads in an initial payload and parses it to EDN, checks to see if it
   conforms to the spec.
   Then reads the mim.edn file in the payload's cwd, checks to make sure it
   matches the spec.
   Then executes the task."
  []
  (let [payload (parse-payload (read-line))]
    ;; validate payload
    (log/info "Got payload:" payload)
    (case (:command payload)
      :from-edn (commands/from-edn payload)
      :eval (commands/eval-form payload)
      :stop (commands/stop!)
      (commands/exit! 1))
    ))

(defstate socket-server
  :start (do
           (spit (fs/expand-home "~/.mim/pid") (mim.pid/current))
           (socket/start-server {:port 1234
                               :name "mim-socket"
                               :accept 'mim.core/server-handler
                               :server-daemon false}))
  :stop (do
          (socket/stop-server "mim-socket")
          (fs/delete (fs/expand-home "~/.mim/pid"))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [mim-folder (fs/expand-home "~/.mim")]
    ;; Initialize the mim folder
    (when-not (fs/exists? mim-folder)
      (fs/mkdir mim-folder))
    (mount/start)
    (log/info "started")
    ))

(comment (mount/stop)
         (fs/exists? (fs/expand-home "~/.mim")))
