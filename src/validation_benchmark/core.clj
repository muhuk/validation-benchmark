(ns validation-benchmark.core
  (:require [clojure.java.io :refer [writer]]
            [clojure.pprint :refer [pprint]]
            [criterium.core :as criterium]
            [incanter.core :as incanter]
            [incanter.charts :as chart]
            [table.core :refer [table]])
  (:gen-class))


(declare make-chart
         run-test
         test-lib)


(def quick? true)


(def alternatives {:annotate 'validation-benchmark.annotate
                   :herbert 'validation-benchmark.herbert
                   :placebo 'validation-benchmark.placebo
                   :schema 'validation-benchmark.schema})


(def bench-out-path "criterium.output")


(def tests
  {'nil-allowed-bool [[true false nil]
                      [1 "x" 'y :z [] #{} {}]]
   'nil-allowed-number [[0
                         1
                         -1
                         42
                         0.5
                         -1.41421
                         22/7
                         3.14159265358M
                         36786883868216818816N
                         nil]
                        [true "x" 'y :z [] #{} {}]]
   'nil-allowed-string [["Foo"
                         ""
                         nil
                         "SimpleBeanFactoryAwareAspectInstanceFactory"
                         "AbstractSingletonProxyFactoryBean"
                         "TransactionAwarePersistenceManagerFactoryProxy"]
                        [true 'y :z [] #{} {}]]})


(defn check-results [results]
  (println "Checking results.")
  (let [lib-names (keys alternatives)
        test-names (keys tests)]
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


(defn final-summary [results chart-path]
  (println "Summary:")
  (let [summary (->> (for [[lib-name test-results] results
                           [test-name {:keys [mean]}] test-results]
                       [test-name lib-name (* mean 1e9)])
                     (sort-by (fn [[t l _]] [t l])))]
    (table (->> summary
                (map (fn [[t l m]] [t l (format "%10.3f" m)]))
                (into [["Test name" "Library" "Mean (ns)"]])))
    (make-chart (incanter/dataset [:test :lib :timing] summary) chart-path)))


(defn make-chart [data filename]
  (incanter/with-data
    (->> data
         (incanter/add-derived-column :test-lib
                                      [:test :lib]
                                      #(format "%s %s" %1 %2))
         (incanter/add-derived-column :timing-base
                                      [:test]
                                      #(->> data
                                            (incanter/$where {:test % :lib :placebo})
                                            (incanter/$ [:timing])
                                            (incanter/$ 0)))
         (incanter/add-derived-column :timing-extra
                                      [:timing :timing-base]
                                      -)
         (incanter/$where (fn [row]
                            (not= (row :lib) :placebo)))
         (incanter/$ [:test-lib :timing-base :timing-extra]))
    (-> (chart/stacked-bar-chart :test-lib
                                 :timing-base
                                 :series-label "base"
                                 :title "Performance Comparison"
                                 :x-label "Test & library"
                                 :y-label "Timing in nanoseconds"
                                 :legend true
                                 :vertical false)
        (chart/add-categories :test-lib
                              :timing-extra
                              :series-label "extra")
        (incanter/save filename
                       :width 770
                       :height 800))))


(defn run-benchmarks [alternatives tests]
  (println "Running benchmarks.")
  (->> (for [[lib-name lib-ns] alternatives]
         (do
           (println " " lib-name)
           [lib-name (test-lib quick? lib-ns tests)]))
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


(defn test-lib [quick? lib-ns tests]
  (let [publics (ns-publics lib-ns)
        wrapper (some-> publics
                        (get 'wrapper)
                        (var-get))]
    (assert (some? wrapper)
            (str "No wrapper in" lib-ns))
    (->> (for [[test-name [valids invalids]] tests
               [test-name' test-data valid?] (map vector
                                           (map #(-> test-name
                                                     (name)
                                                     (str %)
                                                     (symbol))
                                                ["-valid" "-invalid"])
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
  (doseq [[_ lib-ns] alternatives]
    (require [lib-ns]))
  ;; (final-summary (read-string (slurp "results.edn")) "chart.png")
  (let [results-path "results.edn"
        chart-path "chart.png"
        results (run-benchmarks alternatives tests)]
    (check-results results)
    (save-results results results-path)
    (final-summary results chart-path)))
