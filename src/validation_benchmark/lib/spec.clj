(ns validation-benchmark.lib.spec
  (:require [clojure.spec :as s]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import [validation_benchmark.common Person]))


(s/def ::age int?)
(s/def ::name string?)
(s/def ::saiyan? (s/or :t true? :f false?))
(s/def ::keyword-set (s/every keyword :kind set?))
(s/def ::nilable-saiyan? (s/nilable ::saiyan?))
(s/def ::nilable-number (s/nilable number?))
(s/def ::nilable-string (s/nilable string?))
(s/def ::person (s/keys :req-un [::name ::saiyan? ::age]))
(s/def ::Person? #(instance? Person %))
(s/def ::range (s/tuple (partial in-range? 0.0 1.0)
                        (partial in-range? 1 10)
                        (partial in-range? 1 100)))
(s/def ::three-tuple
  (s/and coll? (s/conformer vec) (s/tuple keyword? string? number?)))
(s/def ::typed-person (s/and ::Person?
                             (s/keys :req-un [::name ::saiyan? ::age])))
(s/def ::vector-of-two-elements (s/and vector? #(= 2 (count %))))

(defn checker [s]
  (let [spec (s/spec s)]
    #(s/valid? spec %)))

(def atomic-keyword (checker keyword?))
(def atomic-number (checker number?))
(def nil-allowed-bool (checker ::nilable-saiyan?))
(def nil-allowed-number (checker ::nilable-number))
(def nil-allowed-string (checker ::nilable-string))
(def person-map (checker ::person))
(def person-record (checker ::typed-person))
(def primes (checker prime?))
(def range-check (checker ::range))
(def set-of-keywords (checker ::keyword-set))
(def three-tuple (checker ::three-tuple))
(def vector-of-two-elements (checker ::vector-of-two-elements))
(def vector-of-variable-length (checker vector?))

(defn wrapper [f valid?]
  (fn [v]
    (= (f v) valid?)))
