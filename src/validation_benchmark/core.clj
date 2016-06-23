(ns validation-benchmark.core
  (:require [clojure.java.io :refer [writer]]
            [clojure.pprint :refer [pprint]]
            [validation-benchmark.bench :as bench]
            [validation-benchmark.cli :as cli]
            [validation-benchmark.edn :refer [reader->seq
                                              resource-reader]]
            [validation-benchmark.report :refer [create-report]])
  (:gen-class))


(defn assert-result
  "Wraps benchmark function to assert it always returns `true`.

  `msg` is formatted with the arguments passed to benchmark function,
  and used as a custom assertion failure message."
  [f msg]
  (fn [& args]
    (let [r (apply f args)]
      (assert r (format msg args))
      nil)))


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


(defn prepare-benchmarks
  "Builds runtime benchmark data.

  Canonical benchmark data is stored in `tests.edn`. Output of
  this function has the shape below:

      {:<library-name> {<benchmark-name> {:valid {:fn [...]
                                                  :inputs (fn [...] ...)},
                                          :invalid {:fn [...]
                                                    :inputs (fn [...] ...)}}
       ..rest..}

  Benchmark functions are called with each input. They either return `nil`
  or throw an `AssertionError`. Throwing means a defect in the code."
  [alternatives inputs]
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


(defn run-benchmarks
  "Replaces benchmark function & inputs with the results of running the
  benchmark."
  [benchmarks bench-fn]
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


(defn -main
  [& args]
  (let [{:keys [options]} (cli/parse args)
        benchmark-fns {:real bench/real
                       :quick bench/quick
                       :dev bench/dev}
        [{:keys [alternatives
                 groups
                 inputs]}] (reader->seq (resource-reader "tests.edn"))
        results-path "target/results.edn"
        report-path "target/report"]
    (require-alternatives alternatives)
    (when options
      (if (:reuse options)
        (create-report alternatives
                       groups
                       (read-string (slurp results-path))
                       report-path)
        (let [benchmarks (prepare-benchmarks alternatives inputs)
              bench-fn (benchmark-fns (:mode options))
              results (run-benchmarks benchmarks bench-fn)]
          (save-results results results-path)
          (create-report alternatives groups results report-path))))
    (System/exit 0)))
