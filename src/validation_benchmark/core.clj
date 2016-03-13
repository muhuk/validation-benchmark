(ns validation-benchmark.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :refer [reader resource writer]]
            [clojure.pprint :refer [pprint]]
            [criterium.core :as criterium]
            [incanter.core :as incanter]
            [incanter.charts :as chart]
            [table.core :refer [table]])
  (:gen-class))


(declare make-chart
         run-test
         test-lib)


(def bench-out-path "target/criterium.output")
(def quick? true)


(defn check-results [alternatives inputs results]
  (println "Checking results.")
  (let [lib-names (keys alternatives)
        test-names (keys inputs)]
    (doseq [test-name test-names]
      (println " " test-name)
      (let [test-results (->> lib-names
                              (map #(vector %
                                            (get-in results
                                                    [% test-name :results])))
                              (remove (comp nil? second))
                              (into {}))]
        (doseq [lib-name lib-names
                :let [results' (get-in results
                                       [lib-name test-name :results])]]
          (when-not (empty? results')
            (assert (every? #(every? true? %) results')
                    (str "invalid results for " lib-name))))))))


(defn edn-reader [^String path]
  (-> path
      (resource)
      (reader)
      (java.io.PushbackReader.)))


(defn final-summary [groups results chart-path]
  (println "Summary:")
  (let [summary (->> (for [[group fns] groups
                           [lib-name test-results] results
                           valid? [:valid :invalid]]
                       [[group valid?]
                        lib-name
                        (->> test-results
                             (filter (fn [[[n v] _]] (and (= v valid?)
                                                          (contains? fns n))))
                             (map (comp :mean second))
                             (map (partial * 1e9))
                             (reduce +))])
                     ;; (= v :invalid) returns false for :valid,
                     ;; so it's sorted before :invalid.
                     (sort-by (fn [[[n v] l _]] [n (= v :invalid) l])))]
    (table (->> summary
                (map (fn [[t l m]] [t l (format "%10.3f" m)]))
                (into [["Test name" "Library" "Mean (ns)"]])))
    (make-chart (incanter/dataset [:test :lib :timing] summary) chart-path)))


(defn make-chart [data filename]
  (let [grouped-by-lib (->> data
                            (incanter/$group-by [:lib])
                            (map (fn [[k v]] [(:lib k) v]))
                            (sort))
        [fst-lib fst-data] (first grouped-by-lib)
        chart (chart/bar-chart :test
                               :timing
                               :title "Performance Comparison"
                               :x-label "Test & library"
                               :y-label "Timing in nanoseconds"
                               :series-label fst-lib
                               :data fst-data
                               :legend true
                               :vertical false)
        series (rest grouped-by-lib)]
    (-> (loop [chart chart
               [[lib-name lib-data] & r] series]
          (if (some? lib-name)
            (recur (chart/add-categories chart
                                         :test
                                         :timing
                                         :series-label lib-name
                                         :data lib-data)
                   r)
            chart))
        (incanter/save filename
                       :width 770
                       :height 800))))


(defn reader->seq [^java.io.PushbackReader reader]
  (lazy-seq
    (let [v (edn/read {:eof ::EOF} reader)]
      (if (= v ::EOF)
        nil
        (cons v (reader->seq reader))))))


(defn run-benchmarks [alternatives inputs]
  (println "Running benchmarks.")
  (->> (for [[lib-name lib-ns] alternatives]
         (do
           (println " " lib-name)
           [lib-name (test-lib quick? lib-ns inputs)]))
       (into {})))


(defn run-test [quick? test-fn test-data]
  (let [bench (if quick?
                criterium/quick-benchmark*
                criterium/benchmark*)
        opts nil]
    (with-open [bench-out (writer bench-out-path :append true)]
      (binding [*out* bench-out]
        (bench (fn [] (doall (map test-fn test-data))) opts)))))


(defn save-results [results path]
  (println "Saving results.")
  (with-open [w (writer path)]
    (println "  ->" path)
    (pprint results w)))


(defn summarize [results]
  (-> results
      (select-keys [:results
                    :total-time
                    :samples])
      (merge {:system {:os (str (get-in results
                                        [:os-details :name])
                                " "
                                (get-in results
                                        [:os-details :version]))
                       :processors (get-in results
                                           [:os-details
                                            :available-processors])
                       :jvm (str (get-in results
                                         [:runtime-details
                                          :spec-vendor])
                                 " "
                                 (get-in results
                                         [:runtime-details
                                          :spec-name])
                                 " "
                                 (get-in results
                                         [:runtime-details
                                          :vm-version]))
                       :jre (get-in results
                                    [:runtime-details
                                     :java-runtime-version])
                       :clojure (get-in results
                                        [:runtime-details
                                         :clojure-version-string])}
              :mean (get-in results [:mean 0])
              :variance (get-in results [:variance 0])
              :lower-q (get-in results [:lower-q 0])
              :upper-q (get-in results [:upper-q 0])
              :final-gc-time (/ (:final-gc-time results) 1e9)})))


(defn test-lib [quick? lib-ns inputs]
  (let [publics (ns-publics lib-ns)
        wrapper (some-> publics
                        (get 'wrapper)
                        (var-get))]
    (assert (some? wrapper)
            (str "No wrapper in" lib-ns))
    (->> (for [[test-name [valids invalids]] inputs
               [test-name' test-data valid?] (map vector
                                                  (map #(vector test-name %)
                                                       [:valid :invalid])
                                                  [valids invalids]
                                                  [true false])
               :when (seq test-data)]
           (do
             (println "   " test-name')
             [test-name'
              (if-let [test-fn (some-> publics
                                       (get test-name)
                                       (var-get))]
                (summarize (run-test quick?
                                     (wrapper test-fn valid?)
                                     test-data)))]))
         (into {}))))


(defn -main
  [& args]
  (let [[{:keys [alternatives
                 groups
                 inputs]}] (reader->seq (edn-reader "tests.edn"))
        results-path "target/results.edn"
        chart-path "target/chart.png"]
    (doseq [[_ lib-ns] alternatives]
      (require [lib-ns]))
    #_(final-summary groups
                   (read-string (slurp results-path))
                   chart-path)
    (let [results (run-benchmarks alternatives inputs)]
      (check-results alternatives inputs results)
      (save-results results results-path)
      (final-summary groups results chart-path))))
