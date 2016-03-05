(ns validation-benchmark.schema
  (:require [schema.core :as s]))


(defn nil-allowed-bool [v]
  (s/validate (s/maybe s/Bool) v)
  v)


(defn nil-allowed-number [v]
  (s/validate (s/maybe s/Num) v)
  v)


(defn nil-allowed-string [v]
  (s/validate (s/maybe s/Str) v)
  v)
