(ns validation-benchmark.bench
  (:require [clojure.java.io :refer [writer]]
            [criterium.core :as criterium]))


(declare summarize)


(def ^:private tmpfile
  (delay (let [f (java.io.File/createTempFile "benchmark-output-" ".txt")]
           (println "Benchmark output is redirected to" (.getAbsolutePath f))
           f)))


(defmacro stdout->tmpfile [fname & body]
  `(let [file# @tmpfile]
     (with-open [out# (writer file# :append true)]
      (binding [*out* out#]
        ~@body))))


(defn dev [f]
  (let [start (System/nanoTime)]
    (f)
    {:mean (- (System/nanoTime) start)
     :standard-deviation 0.0}))


(defn quick [f]
  (stdout->tmpfile "criterium"
                   (summarize (criterium/quick-benchmark* f nil))))


(defn real [f]
  ;; (stdout->tmpfile "criterium"
  ;;                  (summarize (criterium/benchmark* f nil)))
  (summarize (criterium/benchmark* f nil)))


(defn summarize [results]
  (let [mean (get-in results [:mean 0])
        variance (get-in results [:variance 0])]
    {:mean mean
     :standard-deviation (Math/sqrt variance)}))
