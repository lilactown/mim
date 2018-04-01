(ns mim.core
  (:require [me.raynes.conch.low-level :as sh]
            [mount.core :as mount :refer [defstate]]
            [clojure.core.server :as socket]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.tools.logging :as log])
  (:gen-class))

;; (defn prompt []
;;   (print "=> ")
;;   (flush)
;;   (read-line))

;; This atom keeps the main thread running until it's false
(defonce running? (atom true))

(defn parse-payload
  "Parses a string payload into EDN"
  [payload]
  (let [parsed (clojure.edn/read-string payload)
        ;; get args as a vector
        args (str/split (:args parsed) #" ")]
    (assoc parsed :args args)))

(defn read-config
  "Reads a mim EDN file and returns its contents"
  [path]
  (with-open [r (io/reader path)]
    (edn/read (java.io.PushbackReader. r))))


(defn from-config
  "Executes a form that is defined in a mim EDN file"
  [{:keys [cwd args]}]
  (let [config (read-config (str cwd "/mim.edn"))]
    (log/info "Read mim.edn:" config)
    ;; execute task
    (let [key-path (map keyword args)
          cmd (get-in config key-path)
          ns-cmd `(do (require 'mim)
                      ~cmd)]
      (log/info (str "Executing `" cmd "`"))
      (try
        (println (eval ns-cmd))
        (println 0)
        (catch Exception e
          (println (str "An error occurred: " (.getMessage e)))
          (println 1))))))

(defn server-handler
  "Handles an incoming socket request.
   Reads in an initial payload and parses it to EDN, checks to see if it
   conforms to the spec.
   Then reads the mim.edn file in the payload's cwd, checks to make sure it
   matches the spec.
   Then executes the task."
  [& args]
  (let [payload (parse-payload (read-line))]
    ;; validate payload
    (log/info "Got payload:" payload)
    (case (:command payload)
      :from-config (from-config payload)
      (println "Invalid command")
      (println 1))
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
    (println "started")
    ;; Initialize the mim folder
    (when-not (fs/exists? mim-folder)
      (fs/mkdir mim-folder))
    (loop [continue @running?]
      (when continue
        (recur @running?)))
    ))

(comment (mount/stop)
         (fs/exists? (fs/expand-home "~/.mim")))
