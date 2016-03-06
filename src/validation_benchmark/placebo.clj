(ns validation-benchmark.placebo)


(defn nil-allowed-bool [v] nil)


(defn nil-allowed-number [v] nil)


(defn nil-allowed-string [v] nil)


(defn wrapper [f]
  (fn [v _]
    (f v)
    true))
