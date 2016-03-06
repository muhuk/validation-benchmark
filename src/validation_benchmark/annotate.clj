(ns validation-benchmark.annotate
  (:require [annotate.core :as ann]
            [annotate.types :as types]))


(defn nil-allowed-bool [v]
  (ann/check (types/Nilable Boolean) v))


(defn nil-allowed-number [v]
  (ann/check (types/Nilable types/Num) v))


(defn nil-allowed-string [v]
  (ann/check (types/Nilable String) v))


(defn wrapper [f valid?]
  (fn [v]
    (= (nil? (f v)) valid?)))
