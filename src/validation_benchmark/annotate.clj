(ns validation-benchmark.annotate
  (:require [annotate.core :as ann]
            [annotate.types :as types]))


(defn nil-allowed-bool [v]
  (ann/check (types/Nilable Boolean) v)
  v)


(defn nil-allowed-number [v]
  (ann/check (types/Nilable types/Num) v)
  v)


(defn nil-allowed-string [v]
  (ann/check (types/Nilable String) v)
  v)
