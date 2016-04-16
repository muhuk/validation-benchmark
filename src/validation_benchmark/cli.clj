(ns validation-benchmark.cli
  (:require [clojure.string :refer [lower-case]]
            [clojure.tools.cli :refer [parse-opts]]))


(def spec
  [[nil "--mode MODE" "Benchmark mode: real | quick | dev"
    :default :real
    :parse-fn (comp keyword lower-case)
    :validate [#{:real :quick :dev} "Mode must be real, quick or dev."]]
   ["-h" "--help" "Print this."]])


(defn parse [args]
  (let [{:keys [arguments
              options
              errors
              summary]} (parse-opts args spec)]
  (cond
    (seq errors) (doseq [e errors] (println e))
    (:help options) (println summary)
    :else {:arguments arguments,
           :options options})))
