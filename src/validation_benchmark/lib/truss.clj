(ns validation-benchmark.lib.truss
  "https://github.com/ptaoussanis/truss"
  (:require [taoensso.truss :as truss :refer (have have? have!)]
            [validation-benchmark.common :refer [in-range? prime?]])
  (:import  [validation_benchmark.common Person]))

;; Just return falsey on errors, don't throw.
;; Truss is optimized for passing cases (it's not normally used as a
;; validator but assertion tool), so this is a little strange:
(truss/set-error-fn! nil)

(defn- -boolean? [x] (instance? Boolean x))
(defn- person?   [x] (instance? Person  x))
(defn- have-person-kvs? [x]
  (and
   (have? string?  (:name    x))
   (have? boolean? (:saiyan? x))
   (have? integer? (:age     x))))

;;;;

(defn atomic-keyword     [x] (have? keyword? x))
(defn atomic-number      [x] (have? number?  x))
(defn nil-allowed-bool   [x] (have? [:or nil? -boolean?] x))
(defn nil-allowed-number [x] (have? [:or nil? number?] x))
(defn nil-allowed-string [x] (have? [:or nil? string?] x))
(defn person-map         [x] (and (have? map?    x) (have-person-kvs? x)))
(defn person-record      [x] (and (have? person? x) (have-person-kvs? x)))
(defn primes             [x] (have? prime? x))

(let [in-range1? (partial in-range? 0.0 1.0)
      in-range2? (partial in-range? 1   10)
      in-range3? (partial in-range? 1   100)]

  (defn range-check [x]
    (and
     (have? vector? x)
     (have? #(= % 3) (count x))
     (let [[x1 x2 x3] x]
       (and
        (have? in-range1? x1)
        (have? in-range2? x2)
        (have? in-range3? x3))))))

(defn set-of-keywords [x]
  (and
   (have? set? x)
   (have? keyword? :in x)))

(defn three-tuple [x]
  (and
   (have? sequential? x)
   (have? #(= % 3) (count x))
   (let [[x1 x2 x3] x]
     (have? keyword? x1)
     (have? string?  x2)
     (have? number?  x3))))

(defn vector-of-two-elements [x]
  (and
   (have? vector? x)
   (have? #(= % 2) (count x))))

(defn vector-of-variable-length [x] (have? vector? x))

;;;;

(defn wrapper [f valid?]
  (fn [v]
    (if (f v) ; Pass
      valid?
      (not valid?))))
