(ns validation-benchmark.report
  (:require [table.core :refer [table]]
            [validation-benchmark.chart :refer [make-chart]])
  (:gen-class))


(defn create-report [groups results chart-path]
  (println "Summary:")
  (let [summary (->> (for [[group fns] groups
                           [lib-name lib-data] results
                           valid? [:valid :invalid]]
                       [[group valid?]
                        lib-name
                        (->> lib-data
                             (filter (comp (partial contains? fns) first))
                             (vals)
                             (map valid?)
                             (map (comp (partial * 1e9) :mean))
                             (apply +))])
                     (filter (comp some? last))
                     ;; (= v :invalid) returns false for :valid,
                     ;; so it's sorted before :invalid.
                     (sort-by (fn [[[n v] l _]] [n (= v :invalid) l])))]
    (table (->> summary
                (map (fn [[t l m]] [t l (format "%10.3f" m)]))
                (into [["Test name" "Library" "Mean (ns)"]])))
    (make-chart summary chart-path)))
