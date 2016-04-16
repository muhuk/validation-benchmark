(ns validation-benchmark.lib.herbert
  (:require [miner.herbert :as h]
            [validation-benchmark.common :refer [prime?]]))


(def atomic-keyword (h/conform 'kw))


(def atomic-number (h/conform 'num))


(def nil-allowed-bool (h/conform '(or bool nil)))


(def nil-allowed-number (h/conform '(or num nil)))


(def nil-allowed-string (h/conform '(or str nil)))


(def person-map (h/conform '{:name str, ':saiyan? bool, :age int}))


(def person-record (h/conform '(tag validation-benchmark.common/Person
                                    {:name str, ':saiyan? bool, :age int})))


(def primes (h/conform '(pred prime?)))


(def set-of-keywords (h/conform '#{kw*}))


(def three-tuple (h/conform '(seq kw str num)))


(def vector-of-two-elements (h/conform '(vec any any)))


(def vector-of-variable-length   (h/conform 'vec))


(defn wrapper [f valid?]
  (fn [v]
    (= (boolean (f v))
       valid?)))
