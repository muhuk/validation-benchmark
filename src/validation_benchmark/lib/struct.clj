(ns validation-benchmark.lib.struct
  (:require [struct.core :as st]
            [validation-benchmark.common :refer [prime?]])
  (:import [validation_benchmark.common Person]))

(defn atomic-keyword [v]
  (st/valid-single? v (assoc st/keyword :optional false)))

(defn atomic-number [v]
  (st/valid-single? v (assoc st/number :optional false)))

(defn nil-allowed-bool [v]
  (st/valid-single? v st/boolean))

(defn nil-allowed-number [v]
  (st/valid-single? v st/number))

(defn nil-allowed-string [v]
  (st/valid-single? v st/string))

(def ^:const person-schema
  {:name (assoc st/string :optional false)
   :saiyan? (assoc st/boolean :optional false)
   :age (assoc st/integer :optional false)})

(defn person-map [v]
  (st/valid? v person-schema))

(defn person-record [v]
  (and (instance? Person v)
       (st/valid? v person-schema)))

(def ^:const primes-validator
  {:optional false
   :validate prime?})

(defn primes [v]
  (st/valid-single? v primes-validator))

(defn set-of-keywords [v]
  (st/valid-single? v [st/set [st/every keyword?]]))

(defn wrapper [f valid?]
  (fn [v]
    (= (f v) valid?)))
