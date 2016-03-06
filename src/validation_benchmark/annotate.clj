(ns validation-benchmark.annotate
  (:require [annotate.core :as ann]
            [annotate.types :as types]))


(defn nil-allowed-bool [v]
  (ann/check (types/Nilable Boolean) v))


(defn nil-allowed-number [v]
  (ann/check (types/Nilable types/Num) v))


(defn nil-allowed-string [v]
  (ann/check (types/Nilable String) v))


(defn wrapper [f]
  (fn [v valid?]
    (= (try
         (f v)
         true
         (catch Exception e false))
       valid?)))
