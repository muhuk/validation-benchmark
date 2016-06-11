(ns validation-benchmark.lib.spec
  (:require [clojure.spec :as s]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import [validation_benchmark.common Person]))


(s/def ::name string?)
(s/def ::saiyan? (s/or :t true? :f false?))
(s/def ::age integer?)


(defn atomic-keyword [v]
  (s/conform keyword?  v))


(defn atomic-number [v]
  (s/conform number? v))


(s/def ::nilable-saiyan? (s/nilable (s/or :t true? :f false?)))
(defn nil-allowed-bool [v]
  (s/conform ::nilable-saiyan? v))


(s/def ::nilable-number (s/nilable number?))
(defn nil-allowed-number [v]
  (s/conform ::nilable-number v))


(s/def ::nilable-string (s/nilable string?))
(defn nil-allowed-string [v]
  (s/conform ::nilable-string v))


(s/def ::person (s/keys :req-un [::name ::saiyan? ::age]))
(defn person-map [v]
  (s/conform ::person v))


(s/def ::Person? #(instance? Person %))
(s/def ::typed-person (s/and ::Person?
                        (s/keys :req-un [::name ::saiyan? ::age])))
(defn person-record [v]
  (s/conform ::typed-person
             v))


(defn primes [v]
  (s/conform prime? v))


(s/def ::range (s/tuple (partial in-range? 0.0 1.0)
                 (partial in-range? 1 10)
                 (partial in-range? 1 100)))
(defn range-check [v]
  (s/conform ::range v))


(s/def ::keyword-set (s/and set?
                       (s/coll-of keyword? #{})))
(defn set-of-keywords [v]
  (s/conform ::keyword-set v))


(s/def ::three-tuple
  (s/and coll?
    #(= (count %) 3)
    #(keyword? (first %))
    #(string? (second %))
    #(number? (nth % 2))))

(defn three-tuple [v]
  ;; (s/conform (s/tuple keyword? string? number?) v)
  (s/conform ::three-tuple v))


(s/def ::vector-of-two-elements (s/and (s/coll-of nil [])
                                  (s/tuple nil nil)))

(defn vector-of-two-elements [v]
  (s/conform ::vector-of-two-elements v))


(defn vector-of-variable-length [v]
  (s/conform vector? v))

(defn wrapper [f valid?]
  (fn [v]
    (= (not= (f v)
             :clojure.spec/invalid)
       valid?)))
