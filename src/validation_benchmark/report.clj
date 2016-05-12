(ns validation-benchmark.report
  (:require [clojure.java.io :refer [make-parents writer]]
            [clj-time.format :as format-time]
            [clj-time.local :as local-time]
            [hiccup.page :refer [html5]]
            [validation-benchmark.chart :refer [make-chart]])
  (:gen-class))


(declare render-html)


(defn create-report [groups results report-path]
  (let [current-directory (System/getProperty "user.dir")
        html-path (str report-path "/index.html")
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
    (println "Summary: "
             (str "file://" current-directory "/" html-path))
    (make-parents html-path)
    (with-open [w (writer html-path)]
      (.write w (render-html summary)))
    (make-chart summary chart-path)))


(defn- about []
  [:div
   [:h2 {:id "about"} "About"]
   [:div "Benchmark for Clojure validation libraries."]
   [:div
    [:a {:href "https://github.com/muhuk/validation-benchmark"}
     "Source code"]]])


(defn- fork-me []
  [:a {:href "https://github.com/muhuk/validation-benchmark"}
   [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
          :src "https://camo.githubusercontent.com/a6677b08c955af8400f44c6298f40e7d19cc5b2d/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677261795f3664366436642e706e67"
          :alt "Fork me on GitHub"
          :data-canonical-src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"}]])


(defn- performance-graph []
  [:div
   [:h2 {:id "performance-graph"} "Performance Graph"]
   ;; TODO: Don't hardcode filename.
   [:img {:src "chart.png"}]])


(defn- relative-performance-table [summary]
  [:div
   [:h2 {:id "relative-performance-table"} "Relative performance table"]
   (->> (for [[t l m] summary]
          [:tr
           [:td (pr-str t)]
           [:td (pr-str l)]
           [:td (format "%10.3f" m)]])
        (into [:table {:class "table"}
               [:tr
                [:th "Test name"]
                [:th "Library"]
                [:th "Mean (ns)"]]]))])


(defn- render-html [summary]
  (html5 [:head
          [:link {:rel "stylesheet"
                  :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
                  :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
                  :crossorigin "anonymous"}]
          [:link {:rel "stylesheet"
                  :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
                  :integrity "sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r"
                  :crossorigin "anonymous"}]
          [:script {:src "https://code.jquery.com/jquery-2.2.3.min.js"}]
          [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
                    :integrity "sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
                    :crossorigin "anonymous"}]]
         [:body {:data-spy "scroll"
                 :data-target "#navbar"}
          (fork-me)
          [:div {:class "container"}
           [:div {:id "top" :class "row"}
            [:div {:class "col-md-12"}
             [:div {:class "page-header"}
              [:h1
               "validation-benchmark "
               [:small
                "Updated at "
                (format-time/unparse (format-time/formatter "YYYY-MM-dd")
                                     (local-time/local-now))]]]]]
           [:div {:class "row"}
            [:div {:class "col-md-10"}
             (about)
             (relative-performance-table summary)
             (performance-graph)]
            [:div {:id "navbar"
                   :class "col-md-2"}
             [:ul {:class "nav nav-pills nav-stacked"
                   :data-spy "affix"}
              [:li [:a {:href "#top"} [:span {:class "glyphicon glyphicon-triangle-top"}] " Top"]]
              [:li [:a {:href "#about"} [:span {:class "glyphicon glyphicon-triangle-right"}] " About"]]
              [:li [:a {:href "#relative-performance-table"} [:span {:class "glyphicon glyphicon-triangle-right"}] " Relative performance table"]]
              [:li [:a {:href "#performance-graph"} [:span {:class "glyphicon glyphicon-triangle-right"}] " Performance Graph"]]]]]]]))
