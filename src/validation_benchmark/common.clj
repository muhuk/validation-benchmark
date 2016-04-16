(ns validation-benchmark.common)


(defrecord Person [name saiyan? age])


(defn prime? [n]
  (let [primes #{2, 3, 5, 7, 11,
                 13, 17, 19, 23, 29,
                 31, 37, 41, 43, 47,
                 53, 59, 61, 67, 71,
                 73, 79, 83, 89, 97}]
    (boolean (primes n))))


(defn in-range? [minimum maximum v]
  (<= minimum v maximum))
