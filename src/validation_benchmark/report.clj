(ns validation-benchmark.report
  (:require [clj-time.format :as format-time]
            [clj-time.local :as local-time]
            [clojure.java.io :as io]
            [hiccup.page :refer [html5]]
            [validation-benchmark.chart :refer [make-chart]])
  (:gen-class))


(declare calculate-summary
         calculate-relative-performance
         lib-info
         render-html)


(def ga-js
  (-> (io/resource "js/ga.js")
      (io/file)
      (slurp)))


(defn create-report [alternatives groups results report-path]
  (let [current-directory (System/getProperty "user.dir")
        performance-graph-filename "chart.png"
        html-path (str report-path "/index.html")
        chart-path (str report-path "/" performance-graph-filename)
        summary (calculate-summary groups results)
        relative-performance (calculate-relative-performance summary)
        libraries (lib-info alternatives)]
    (println "Summary: "
             (str "file://" current-directory "/" html-path))
    (io/make-parents html-path)
    (with-open [w (io/writer html-path)]
      (.write w (render-html libraries
                             performance-graph-filename
                             relative-performance)))
    (make-chart summary chart-path)))


(defn calculate-relative-performance [summary]
  (let [last-of-each (partial mapv last)
        normalize #(let [m (apply min %)]
                     (mapv (fn [k] [k (/ k m)]) %))
        group-by-first (partial group-by first)
        f (comp normalize last-of-each)
        rows (->> summary
                  (group-by-first)
                  (map (fn [[k v]] [k (f v)]))
                  (into (sorted-map)))
        columns (->> summary
                     (group-by-first)
                     (vals)
                     (first)
                     (mapv second))]        
    [columns rows]))


(defn calculate-summary [groups results]
  (->> (for [[group fns] groups
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
       (sort-by (fn [[[n v] l _]] [n (= v :invalid) l]))))


(defn dependencies []
  (->> (slurp "project.clj")
       (read-string)
       (drop 3)
       (apply hash-map)
       (:dependencies)))


(defn lib-info [alternatives]
  (let [all-deps (dependencies)
        alt-names (assoc (into {} (map #(vector % %) (keys alternatives)))
                         :spec :clojure)
        lib-deps (reduce (fn [acc [n _ :as dep]]
                           (let [n' (keyword (name n))]
                             (if-let [alt (reduce (fn [_ [nme lib]]
                                                    (when (= n' lib)
                                                      (reduced nme)))
                                                  nil
                                                  alt-names)]
                               (assoc acc alt dep)
                               acc)))
                         {}
                         all-deps)]
    (sort-by first (vec lib-deps))))


(defn- about [libraries]
  [:div
   [:h2 {:id "about"} "About"]
   [:p "Benchmark for Clojure validation libraries."]
   [:p
    [:a {:href "https://github.com/muhuk/validation-benchmark"}
     "Source code"]]
   [:p "Following libraries are benchmarked:"]
   (into [:ul]
         (for [[nme dep] libraries]
           [:li
            [:strong nme]
            " ("
            [:code (str dep)]
            ")"]))])


(defn- fork-me []
  [:a {:href "https://github.com/muhuk/validation-benchmark"}
   [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
          :src "https://camo.githubusercontent.com/a6677b08c955af8400f44c6298f40e7d19cc5b2d/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677261795f3664366436642e706e67"
          :alt "Fork me on GitHub"
          :data-canonical-src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"}]])


(defn- performance-graph [performance-graph-filename]
  [:div
   [:h2 {:id "performance-graph"} "Performance Graph"]
   [:img {:src performance-graph-filename}]])


(defn- relative-performance-table [[columns rows]]
  (let [header (->> columns (map (partial vector :th))
                    (into [:tr [:th "Test"]]))]
    [:div
     [:h2 {:id "relative-performance-table"} "Relative performance table"]
     (into [:table {:class "table table-bordered table-hover"} header]
           (for [[k vs] rows]
             (into [:tr [:td [:samp (pr-str k)]]]
                   (map (fn [[abs rel]]
                          [:td
                           {:class (cond
                                     (<= rel 1.05) "success"
                                     (<= rel 3.0) "warning"
                                     :else "danger")
                            :title (str "Absolute timing is "
                                        (format "%.0f" abs)
                                        " ns.")}
                           (format "%.3f" rel)]) vs))))]))


(defn- render-html [libraries
                    performance-graph-filename
                    relative-performance]
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
             (about libraries)
             (relative-performance-table relative-performance)
             (performance-graph performance-graph-filename)]
            [:div {:id "navbar"
                   :class "col-md-2"}
             [:ul {:class "nav nav-pills nav-stacked"
                   :data-spy "affix"}
              [:li [:a {:href "#top"} [:span {:class "glyphicon glyphicon-triangle-top"}] " Top"]]
              [:li [:a {:href "#about"} [:span {:class "glyphicon glyphicon-triangle-right"}] " About"]]
              [:li [:a {:href "#relative-performance-table"} [:span {:class "glyphicon glyphicon-triangle-right"}] " Relative performance table"]]
              [:li [:a {:href "#performance-graph"} [:span {:class "glyphicon glyphicon-triangle-right"}] " Performance Graph"]]]]]
           [:div {:class "row"}
            [:div {:class "col-md-10"
                   :style "margin-bottom: 6em;"}
             "&nbsp;"]]
           ga-js]]))
