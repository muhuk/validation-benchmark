(ns validation-benchmark.herbert
  (:require [miner.herbert :as h]))


(defn nil-allowed-bool [v]
  (h/conforms? '(or bool nil) v))


(defn nil-allowed-number [v]
  (h/conforms? '(or num nil) v))


(defn nil-allowed-string [v]
  (h/conforms? '(or str nil) v))


(defn wrapper [f valid?]
  (fn [v]
    (= (f v)
       valid?)))
