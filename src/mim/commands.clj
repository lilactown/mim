(ns mim.commands
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [mim.state :as state]))

(defn- read-config
  "Reads a mim EDN file and returns its contents"
  [path]
  (with-open [r (io/reader path)]
    (edn/read (java.io.PushbackReader. r))))

(defn from-edn
  "Executes a form that is defined in a mim EDN file"
  [{:keys [cwd args]}]
  (let [config (read-config (str cwd "/mim.edn"))]
    (log/info "Read mim.edn:" config)
    ;; execute task
    (let [key-path (map keyword args)
          cmd (get-in config key-path)
          ;; the command runs in a separate thread that resides in the
          ;; clojure.core namespace. we need to require the mim ns to access
          ;; e.g. mim/task
          ns-cmd `(do (require 'mim)
                      ~cmd)]
      (log/info (str "Executing `" cmd "`"))
      (try
        (println (eval ns-cmd))
        (println 0)
        (catch Exception e
          (println (str "An error occurred: " (.getMessage e)))
          (println 1))))))

(defn eval-form [{:keys [form in-ns]}]
  (try
    (println (eval form))
    (println 0)
    (catch Exception e
      (println (str "An error occurred: " (.getMessage e)))
      (println 1))))

(defn stop
  "Stops the server"
  []
  (println "Stopping server.")
  (reset! mim.state/running? false))

(comment mim.core/running?)
