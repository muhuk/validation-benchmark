(ns validation-benchmark.lib.annotate
  (:require [annotate.core :as ann]
            [annotate.types :as types]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import [validation_benchmark.common Person]))


(defn atomic-keyword [v]
  (ann/check types/Keyword v))


(defn atomic-number [v]
  (ann/check types/Num v))


(defn nil-allowed-bool [v]
  (ann/check (types/Nilable Boolean) v))


(defn nil-allowed-number [v]
  (ann/check (types/Nilable types/Num) v))


(defn nil-allowed-string [v]
  (ann/check (types/Nilable String) v))


(defn person-map [v]
  (ann/check {:name String, :saiyan? Boolean, :age types/Int} v))


(defn person-record [v]
  (ann/check (types/I Person
                      {:name String, :saiyan? Boolean, :age types/Int}) v))


(defn primes [v]
  (ann/check (types/Pred prime?) v))


(defn range-check [v]
  (ann/check [(types/Pred (partial in-range? 0.0 1.0))
              (types/Pred (partial in-range? 1 10))
              (types/Pred (partial in-range? 1 100))] v))


(defn set-of-keywords [v]
  (ann/check #{types/Keyword} v))


(defn three-tuple [v]
  (ann/check (types/U [types/Keyword String types/Num]
                      (list types/Keyword String types/Num)) v))


(defn vector-of-two-elements [v]
  (ann/check (types/I [types/Any] (types/Count 2)) v))


(defn vector-of-variable-length [v]
  (ann/check [types/Any] v))


(defn wrapper [f valid?]
  (fn [v]
    (= (nil? (f v)) valid?)))
