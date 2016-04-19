(ns validation-benchmark.lib.schema
  (:require [schema.core :as s]
            [validation-benchmark.common :refer [in-range?
                                                 prime?
                                                 map->Person]])
  (:import [validation_benchmark.common Person]))


(def atomic-keyword
  (s/checker s/Keyword))


(def atomic-number
  (s/checker s/Num))


(def nil-allowed-bool
  (s/checker (s/maybe s/Bool)))


(def nil-allowed-number
  (s/checker (s/maybe s/Num)))


(def nil-allowed-string
  (s/checker (s/maybe s/Str)))


(def person-map
  (s/checker {:name s/Str, :saiyan? s/Bool, :age s/Int}))


(def person-record
  (s/checker (s/record Person
                       {:name s/Str, :saiyan? s/Bool, :age s/Int}
                       map->Person)))


(def primes
  (s/checker (s/pred prime?)))


(def range-check
  (s/checker [(s/one (s/pred (partial in-range? 0.0 1.0)) "f")
              (s/one (s/pred (partial in-range? 1 10)) "g")
              (s/one (s/pred (partial in-range? 1 100)) "h")]))


(def set-of-keywords
  (s/checker #{s/Keyword}))


(def three-tuple
  (s/checker [(s/one s/Keyword "k")
              (s/one s/Str "s")
              (s/one s/Num "n")]))


(def vector-of-two-elements
  (s/checker [(s/one s/Any "first") (s/one s/Any "second")]))


(def vector-of-variable-length
  (s/checker (s/conditional vector? [s/Any])))


(defn wrapper [f valid?]
  (fn [v]
    (= (nil? (f v)) valid?)))
