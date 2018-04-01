(ns mim.core
  (:require [me.raynes.conch.low-level :as sh]
            [mount.core :as mount :refer [defstate]]
            [clojure.core.server :as socket]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [mim.commands :as commands])
  (:gen-class))

;; This atom keeps the main thread running until it's false
(def running? (atom true))

(defn keep-running
  "Keeps the main thread running"
  []
  (loop [continue @running?]
    (when continue
      (recur @running?))))

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
      :from-edn (commands/from-config payload)
      :eval (commands/eval-form payload)
      :stop (commands/stop)
      (do (println "Invalid command")
          (println 1)))
    ))

(defstate socket-server
  :start (socket/start-server {:port 1234
                               :name "mim-socket"
                               :accept 'mim.core/server-handler})
  :stop (socket/stop-server "mim-socket"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [mim-folder (fs/expand-home "~/.mim")]
    (mount/start)
    (log/info "started")
    ;; Initialize the mim folder
    (when-not (fs/exists? mim-folder)
      (fs/mkdir mim-folder))
    (keep-running)
    ))

(comment (mount/stop)
         (fs/exists? (fs/expand-home "~/.mim")))
