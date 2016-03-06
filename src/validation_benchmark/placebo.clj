(ns validation-benchmark.placebo)


(defn nil-allowed-bool [v] nil)


(defn nil-allowed-number [v] nil)


(defn nil-allowed-string [v] nil)


(defn wrapper [f _]
  (fn [v]
    (f v)
    true))
