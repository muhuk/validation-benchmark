(ns validation-benchmark.chart
  (:require [incanter.core :as incanter]
            [incanter.charts :as chart]))


(defn bar-chart [title
                 x-label
                 y-label
                 x-col
                 y-col
                 series]
  (let [[[fst-label fst-data] & r] series
        c (chart/bar-chart x-col
                           y-col
                           :title title
                           :x-label x-label
                           :y-label y-label
                           :legend true
                           :vertical false
                           :series-label fst-label
                           :data fst-data)]
    (doseq [[lbl data] r]
      (chart/add-categories c
                            x-col
                            y-col
                            :series-label lbl
                            :data data))
    c))


(defn make-chart [data filename]
  (let [grouped-by-lib (->> data
                            (incanter/dataset [:test :lib :timing])
                            (incanter/$group-by [:lib])
                            (map (fn [[k v]] [(:lib k) v]))
                            (sort))]
    (-> (bar-chart "Performance Comparison"
                   "Test & library"
                   "Timing in nanoseconds"
                   :test
                   :timing
                   grouped-by-lib)
        (incanter/save filename
                       :width 770
                       :height 800))))
