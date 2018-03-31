(ns mim.core
  (:require [me.raynes.conch.low-level :as sh]
            [mount.core :as mount :refer [defstate]]
            [clojure.core.server :as socket]
            [clojure.edn :as edn]
            [clojure.string :as str])
  (:gen-class))

(def tasks {:build "echo hello"})


(comment (def p (sh/proc "bash" "-c" (get-in tasks [:build])))

         (def exit (future (sh/exit-code p)))

         (sh/stream-to-out p :out)

         (sh/destroy p))


(defmulti do-task! class)

(defmethod do-task! java.lang.String
  [cmd]
  (let [p (sh/proc "bash" "-c" cmd)]
            (sh/stream-to-out p :out)
            (sh/destroy p)))

;; (defn prompt []
;;   (print "=> ")
;;   (flush)
;;   (read-line))

(defn parse-payload
  "Parses a string payload into EDN"
  [payload]
  (let [parsed (clojure.edn/read-string payload)
        ;; get args as a vector
        args (str/split (:args parsed) #" ")]
    (assoc parsed :args args)))

(defn server-handler [& args]
  (let [payload (parse-payload (read-line))]
    (println payload)
    (println (type (:cwd payload)))))

(defstate socket-server
  :start (socket/start-server {:port 1234
                               :name "mim-socket"
                               :accept 'mim.core/server-handler})
  :stop (socket/stop-server "mim-socket"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (mount/start))

(comment (do-task! "cat")
         (mount/stop))
