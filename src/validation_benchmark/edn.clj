(ns validation-benchmark.edn
  (:require [clojure.edn :as edn]
            [clojure.java.io :refer [reader resource]]))


(defn reader->seq [^java.io.PushbackReader reader]
  (lazy-seq
    (let [v (edn/read {:eof ::EOF} reader)]
      (if (= v ::EOF)
        nil
        (cons v (reader->seq reader))))))


(defn resource-reader [^String path]
  (-> path
      (resource)
      (reader)
      (java.io.PushbackReader.)))
