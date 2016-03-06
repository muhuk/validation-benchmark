(defproject validation-benchmark "0.1.0-SNAPSHOT"
  :description "Clojure Validation Benchmark"
  :url "https://github.com/muhuk/validation-benchmark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.roomkey/annotate "1.0.1"]
                 [org.clojure/clojure "1.8.0"]
                 [criterium "0.4.3"]
                 [com.velisco/herbert "0.7.0-alpha2"]
                 [prismatic/schema "1.0.4"]
                 [table "0.5.0"]]
  :main ^:skip-aot validation-benchmark.core
  :jvm-opts []
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
