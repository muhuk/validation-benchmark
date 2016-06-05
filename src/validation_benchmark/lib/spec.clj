(ns validation-benchmark.lib.spec
  (:require [clojure.spec :as s]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import [validation_benchmark.common Person]))


(s/def ::name string?)
(s/def ::saiyan? (s/or :t true? :f false?))
(s/def ::age integer?)
(s/def ::Person? #(instance? Person %))


(defn atomic-keyword [v]
  (s/conform keyword?  v))


(defn atomic-number [v]
  (s/conform number? v))


(defn nil-allowed-bool [v]
  (s/conform (s/nilable (s/or :t true? :f false?)) v))


(defn nil-allowed-number [v]
  (s/conform (s/nilable number?) v))


(defn nil-allowed-string [v]
  (s/conform (s/nilable string?) v))


(defn person-map [v]
  (s/conform (s/keys :req-un [::name ::saiyan? ::age]) v))


(defn person-record [v]
  (s/conform (s/and ::Person?
                    (s/keys :req-un [::name ::saiyan? ::age]))
             v))


(defn primes [v]
  (s/conform prime? v))


(defn range-check [v]
  (s/conform (s/tuple (partial in-range? 0.0 1.0)
                      (partial in-range? 1 10)
                      (partial in-range? 1 100))
             v))


(defn set-of-keywords [v]
  (s/conform (s/and set?
                    (s/coll-of keyword? #{})) v))


(defn three-tuple [v]
  ;; (s/conform (s/tuple keyword? string? number?) v)
  (s/conform (s/and coll?
                    #(= (count %) 3)
                    #(keyword? (first %))
                    #(string? (second %))
                    #(number? (nth % 2)))
             v))


(defn vector-of-two-elements [v]
  (s/conform (s/and (s/coll-of nil [])
                    (s/tuple nil nil))
             v))


(defn vector-of-variable-length [v]
  (s/conform vector? v))


(defn wrapper [f valid?]
  (fn [v]
    (= (not= (f v)
             :clojure.spec/invalid)
       valid?)))
