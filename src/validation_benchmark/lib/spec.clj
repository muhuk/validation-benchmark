(ns validation-benchmark.lib.spec
  (:require [clojure.spec :as s]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import [validation_benchmark.common Person]))


(s/def ::age integer?)
(s/def ::name string?)
(s/def ::keyword-set (s/and set?
                            (s/coll-of keyword? #{})))
(s/def ::nilable-saiyan? (s/nilable (s/or :t true? :f false?)))
(s/def ::nilable-number (s/nilable number?))
(s/def ::nilable-string (s/nilable string?))
(s/def ::person (s/keys :req-un [::name ::saiyan? ::age]))
(s/def ::Person? #(instance? Person %))
(s/def ::range (s/tuple (partial in-range? 0.0 1.0)
                        (partial in-range? 1 10)
                        (partial in-range? 1 100)))
(s/def ::saiyan? (s/or :t true? :f false?))
(s/def ::three-tuple
  (s/and coll?
         #(= (count %) 3)
         #(keyword? (first %))
         #(string? (second %))
         #(number? (nth % 2))))
(s/def ::typed-person (s/and ::Person?
                             (s/keys :req-un [::name ::saiyan? ::age])))
(s/def ::vector-of-two-elements (s/and (s/coll-of nil [])
                                       (s/tuple nil nil)))


(defn atomic-keyword [v]
  (s/valid? keyword?  v))


(defn atomic-number [v]
  (s/valid? number? v))


(defn nil-allowed-bool [v]
  (s/valid? ::nilable-saiyan? v))


(defn nil-allowed-number [v]
  (s/valid? ::nilable-number v))


(defn nil-allowed-string [v]
  (s/valid? ::nilable-string v))


(defn person-map [v]
  (s/valid? ::person v))


(defn person-record [v]
  (s/valid? ::typed-person
             v))


(defn primes [v]
  (s/valid? prime? v))


(defn range-check [v]
  (s/valid? ::range v))


(defn set-of-keywords [v]
  (s/valid? ::keyword-set v))



(defn three-tuple [v]
  (s/valid? ::three-tuple v))


(defn vector-of-two-elements [v]
  (s/valid? ::vector-of-two-elements v))


(defn vector-of-variable-length [v]
  (s/valid? vector? v))

(defn wrapper [f valid?]
  (fn [v]
    (= (f v) valid?)))
