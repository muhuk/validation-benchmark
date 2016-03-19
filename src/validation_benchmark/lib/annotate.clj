(ns validation-benchmark.lib.annotate
  (:require [annotate.core :as ann]
            [annotate.types :as types]))


(defn nil-allowed-bool [v]
  (ann/check (types/Nilable Boolean) v))


(defn nil-allowed-number [v]
  (ann/check (types/Nilable types/Num) v))


(defn nil-allowed-string [v]
  (ann/check (types/Nilable String) v))


(defn set-of-keywords [v]
  (ann/check #{types/Keyword} v))


(defn three-tuple [v]
  (ann/check [types/Keyword String types/Num] v))


(defn vector-of-two-elements [v]
  (ann/check (types/I [types/Any] (types/Count 2)) v))


(defn vector-of-variable-length [v]
  (ann/check [types/Any] v))


(defn wrapper [f valid?]
  (fn [v]
    (= (nil? (f v)) valid?)))
