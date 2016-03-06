(ns validation-benchmark.core
  (:require [clojure.java.io :refer [writer]]
            [clojure.pprint :refer [pprint]]
            [criterium.core :as criterium])
  (:gen-class))


(declare run-test
         test-lib)


(def quick? true)


(def alternatives {:annotate 'validation-benchmark.annotate
                   :herbert 'validation-benchmark.herbert
                   :placebo 'validation-benchmark.placebo
                   :schema 'validation-benchmark.schema})


(def bench-out-path "criterium.output")


(def tests
  (letfn [(inputs [valids invalids]
            (->> (concat (map vector valids (repeat true))
                         (map vector invalids (repeat false)))
                 (into [])
                 (shuffle)))]
    {'nil-allowed-bool (inputs [true false nil]
                               [1 "x" 'y :z [] #{} {}])
     'nil-allowed-number (inputs [0
                                  1
                                  -1
                                  42
                                  0.5
                                  -1.41421
                                  22/7
                                  3.14159265358M
                                  36786883868216818816N
                                  nil]
                                 [true "x" 'y :z [] #{} {}])
     'nil-allowed-string (inputs ["Foo"
                                  ""
                                  nil
                                  "SimpleBeanFactoryAwareAspectInstanceFactory"
                                  "AbstractSingletonProxyFactoryBean"
                                  "TransactionAwarePersistenceManagerFactoryProxy"]
                                 [true 'y :z [] #{} {}])}))


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


(defn print-summary [results]
  (println "Summary:")
  (doseq [[lib-name test-results] results]
    (doseq [[test-name {:keys [mean]}] test-results]
      (println
        (if mean
          (format "  %s\t%s\tmean: %.9fs" lib-name test-name mean)
          (format "  %s\t%s\t--" lib-name test-name))))
    (let [total (->> (vals test-results)
                     (map :mean)
                     (filter some?)
                     (apply +))]
      (println (format "  %s total: %.9fs" lib-name total)))
    (println)))


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
        (bench (fn [] (doall (map (partial apply test-fn) test-data))) opts)))))


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
    (->> (for [[test-name test-data] tests]
           (do
             (println "   " test-name)
             [test-name
              (if-let [test-fn (some-> publics
                                       (get test-name)
                                       (var-get))]
                (summarize (run-test quick?
                                     (wrapper test-fn)
                                     test-data)))]))
         (into {}))))


(defn -main
  [& args]
  (doseq [[_ lib-ns] alternatives]
    (require [lib-ns]))
  (let [results-path "results.edn"
        results (run-benchmarks alternatives tests)]
    (check-results results)
    (save-results results results-path)
    (print-summary results)))
