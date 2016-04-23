(ns validation-benchmark.report
  (:require [clojure.java.io :refer [make-parents writer]]
            [clj-time.format :as format-time]
            [clj-time.local :as local-time]
            [hiccup.page :refer [html5]]
            [validation-benchmark.chart :refer [make-chart]])
  (:gen-class))


(declare render-html)


(defn create-report [groups results report-path]
  (println "Summary:")
  (let [html-path (str report-path "/index.html")
        chart-path (str report-path "/chart.png")
        summary (->> (for [[group fns] groups
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
    (make-parents html-path)
    (with-open [w (writer html-path)]
      (.write w (render-html summary)))
    (make-chart summary chart-path)))


(defn- render-html [summary]
  (html5 [:h1 "validation-benchmark"]
         [:h2 "Updated at " (format-time/unparse (format-time/formatter "YYYY-MM-dd")
                                                 (local-time/local-now))]
         (->> (for [[t l m] summary]
                [:tr
                 [:td (pr-str t)]
                 [:td (pr-str l)]
                 [:td (format "%10.3f" m)]])
              (into [:table [:tr
                             [:th "Test name"]
                             [:th "Library"]
                             [:th "Mean (ns)"]]]))
         ;; TODO: Don't hardcode filename.
         [:img {:src "chart.png"}]))
