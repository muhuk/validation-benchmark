(ns validation-benchmark.herbert
  (:require [miner.herbert :as h]))


(defn nil-allowed-bool [v]
  (h/conforms? '(or bool nil) v))


(defn nil-allowed-number [v]
  (h/conforms? '(or num nil) v))


(defn nil-allowed-string [v]
  (h/conforms? '(or str nil) v))


(defn set-of-keywords [v]
  (h/conforms? '#{kw*} v))


(defn three-tuple [v]
  (h/conforms? '(seq kw str num) v))


(defn vector-of-two-elements [v]
  (h/conforms? '(vec any any) v))


(defn vector-of-variable-length [v]
  (h/conforms? '(vec any*) v))


(defn wrapper [f valid?]
  (fn [v]
    (= (f v)
       valid?)))
