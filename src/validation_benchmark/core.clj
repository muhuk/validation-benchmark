(ns validation-benchmark.core
  (:require [clojure.java.io :refer [writer]]
            [clojure.pprint :refer [pprint]]
            [criterium.core :as criterium])
  (:gen-class))


(declare run-test
         test-lib)


(def quick? true)


(def alternatives {:annotate 'validation-benchmark.annotate
                   :schema 'validation-benchmark.schema})


(def tests
  {'nil-allowed-bool [true false nil]
   'nil-allowed-number [0
                        42
                        0.5
                        22/7
                        3.14159265358M
                        36786883868216818816N
                        nil]
   'nil-allowed-string ["Foo" "" nil]})


(defn check-results [results]
  (println "Checking results.")
  (let [lib-names (keys alternatives)
        test-names (keys tests)]
    (doseq [test-name test-names]
      (println " " test-name)
      (let [test-results (->> lib-names
                              (map #(get-in results
                                            [% test-name :results]))
                              (remove nil?))]
        (when-not (empty? test-results)
          (assert (apply = test-results)
                  (str "test results don't match for" test-name)))))))


(defn print-summary [results]
  (println "Summary:")
  (doseq [[lib-name test-results] results]
    (let [total (->> (vals test-results)
                     (map :mean)
                     (filter some?)
                     (apply +))]
      (println (format "  %s total: %.9fs" lib-name total)))
    (doseq [[test-name {:keys [mean]}] test-results]
      (println
        (if mean
          (format "  %s\t%s\tmean: %.9fs" lib-name test-name mean)
          (format "  %s\t%s\t--" lib-name test-name))))))


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
         f (fn [v]
            (try
              (test-fn v)
              (catch Exception e ::exception)))
        opts nil]
    (bench (fn [] (doall (map f test-data))) opts)))


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
  (let [publics (ns-publics lib-ns)]
    (->> (for [[test-name test-data] tests]
           (do
             (println "   " test-name)
             [test-name
              (if-let [test-fn (some-> publics
                                       (get test-name)
                                       (var-get))]
                (summarize (run-test quick?
                                     test-fn
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
