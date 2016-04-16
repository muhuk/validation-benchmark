(ns validation-benchmark.edn
  (:require [clojure.edn :as edn]
            [validation-benchmark.common]
            [clojure.java.io :refer [reader resource]]))


(defn reader->seq [^java.io.PushbackReader reader]
  (let [opts {:eof ::EOF
             :readers {'common/person validation-benchmark.common/map->Person}}]
    (lazy-seq
      (let [v (edn/read opts reader)]
        (if (= v ::EOF)
          nil
          (cons v (reader->seq reader)))))))


(defn resource-reader [^String path]
  (-> path
      (resource)
      (reader)
      (java.io.PushbackReader.)))
