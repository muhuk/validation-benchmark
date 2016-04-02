(ns validation-benchmark.core
  (:require [clojure.java.io :refer [writer]]
            [clojure.pprint :refer [pprint]]
            [criterium.core :as criterium]
            [table.core :refer [table]]
            [validation-benchmark.chart :refer [make-chart]]
            [validation-benchmark.edn :refer [reader->seq
                                              resource-reader]])
  (:gen-class))


(def bench-out-path "target/criterium.output")


(defmacro stdout->file [fname & body]
  `(with-open [out# (writer ~fname :append true)]
      (binding [*out* out#]
        ~@body)))


(defn assert-result [f msg]
  (fn [& args]
    (let [r (apply f args)]
      (assert r (format msg args))
      r)))


(defn final-summary [groups results chart-path]
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


(defn prepare-benchmark-for-lib [lib-name lib-ns test-name [valids invalids]]
  (let [publics (ns-publics lib-ns)
        wrapper (some-> publics
                        (get 'wrapper)
                        (var-get))
        assert-msg "invalid result for lib: %s, test: [%s %s], args: %%s"]
    (assert (some? wrapper)
            (str "No wrapper in" lib-ns))
    (assert (or (seq valids) (seq invalids)))
    (when-let [test-fn (some-> publics
                               (get test-name)
                               (var-get))]
      [test-name
       (->> (for [[kw in valid?] [[:valid valids true]
                                  [:invalid invalids false]]]
              [kw {:inputs in
                   :fn (assert-result (wrapper test-fn valid?)
                                      (format assert-msg
                                              lib-name
                                              kw
                                              test-name))}])
            (into {}))])))


(defn prepare-benchmarks [alternatives inputs]
  (->> (for [[lib-name lib-ns] alternatives]
         [lib-name
          (->> (for [[test-name test-data] inputs]
                 (prepare-benchmark-for-lib lib-name
                                            lib-ns
                                            test-name
                                            test-data))
               (into {}))])
       (into {})))


(defn require-alternatives [alternatives]
  (doseq [[_ lib-ns] alternatives]
      (require [lib-ns])))


(defn run-benchmarks [benchmarks bench-fn]
  (let [flattened (for [[lib-name lib-data] benchmarks
                        [test-name test-data] lib-data
                        [valid? {test-fn :fn inputs :inputs}] test-data]
                    [[lib-name test-name valid?] [test-fn inputs]])]
    (println "Running benchmarks.")
    (loop [benchmarks-with-results benchmarks
           [[k [test-fn test-data]] & r] flattened]
      (if (some? k)
        (do
          (println " " k)
          (recur (->> (fn [] (doall (map test-fn test-data)))
                      (bench-fn)
                      (assoc-in benchmarks-with-results k))
                 r))
        benchmarks-with-results))))


(defn save-results [results path]
  (println "Saving results.")
  (with-open [w (writer path)]
    (println "  ->" path)
    (pprint results w)))


(defn summarize [results]
  (let [mean (get-in results [:mean 0])
        variance (get-in results [:variance 0])]
    {:mean mean
     :standard-deviation (Math/sqrt variance)}))


(defn -main
  [& args]
  (let [[{:keys [alternatives
                 groups
                 inputs]}] (reader->seq (resource-reader "tests.edn"))
        results-path "target/results.edn"
        chart-path "target/chart.png"]
    (require-alternatives alternatives)
    #_(final-summary groups
                   (read-string (slurp results-path))
                   chart-path)
    (let [benchmarks (prepare-benchmarks alternatives inputs)
          quick? false
          bench-fn (let [b (if quick?
                             criterium/quick-benchmark*
                             criterium/benchmark*)
                         opts nil]
                     #(stdout->file bench-out-path
                                    (summarize (b % opts))))
;;           bench-fn (fn [f]
;;                      (let [start (System/nanoTime)]
;;                        (f)
;;                        {:mean (- (System/nanoTime) start)
;;                         :standard-deviation 0.0}))
          results (run-benchmarks benchmarks bench-fn)]
      (save-results results results-path)
      (final-summary groups results chart-path))))
