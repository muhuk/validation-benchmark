(ns validation-benchmark.herbert
  (:require [miner.herbert :as h]))


(defn nil-allowed-bool [v]
  (if-not (h/conforms? '(or bool nil) v)
    (throw (RuntimeException.))
    v))


(defn nil-allowed-number [v]
  (if-not (h/conforms? '(or num nil) v)
    (throw (RuntimeException.))
    v))


(defn nil-allowed-string [v]
  (if-not (h/conforms? '(or str nil) v)
    (throw (RuntimeException.))
    v))
