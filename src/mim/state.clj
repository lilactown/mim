(ns mim.state)

;; This atom keeps the main thread running until it's false
(def running? (atom true))

(defn keep-running
  "Keeps the main thread running"
  []
  (loop [continue @running?]
    (when continue
      (recur @running?))))
