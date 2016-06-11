(ns validation-benchmark.lib.annotate
  (:require [annotate.core :as ann]
            [annotate.types :as types]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import [validation_benchmark.common Person]))


(def nillable-boolean (types/Nilable Boolean))
(def nillable-number (types/Nilable types/Num))
(def nillable-string (types/Nilable String))
(def person-map-schema {:name String, :saiyan? Boolean, :age types/Int})
(def person-record-schema (types/I Person
                                   person-map-schema))
(def pred-prime? (types/Pred prime?))
(def range-schema [(types/Pred (partial in-range? 0.0 1.0))
                   (types/Pred (partial in-range? 1 10))
                   (types/Pred (partial in-range? 1 100))])
(def set-of-keywords-schema #{types/Keyword})
(def three-tuple-schema
  (types/U [types/Keyword String types/Num]
           (list types/Keyword String types/Num)))
(def vector-of-two-elements-schema
  (types/I [types/Any] (types/Count 2)))
(def vector-of-variable-length-schema [types/Any])


(defn atomic-keyword [v]
  (ann/check types/Keyword v))


(defn atomic-number [v]
  (ann/check types/Num v))


(defn nil-allowed-bool [v]
  (ann/check nillable-boolean v))


(defn nil-allowed-number [v]
  (ann/check nillable-number v))


(defn nil-allowed-string [v]
  (ann/check nillable-string v))


(defn person-map [v]
  (ann/check person-map-schema v))


(defn person-record [v]
  (ann/check person-record-schema v))


(defn primes [v]
  (ann/check pred-prime? v))


(defn range-check [v]
  (ann/check range-schema v))


(defn set-of-keywords [v]
  (ann/check set-of-keywords-schema v))


(defn three-tuple [v]
  (ann/check three-tuple-schema v))


(defn vector-of-two-elements [v]
  (ann/check vector-of-two-elements-schema v))


(defn vector-of-variable-length [v]
  (ann/check vector-of-variable-length-schema v))


(defn wrapper [f valid?]
  (fn [v]
    (= (nil? (f v)) valid?)))
