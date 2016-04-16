(defproject validation-benchmark "0.1.0-SNAPSHOT"
  :description "Benchmark for Clojure validation libraries."
  :url "https://github.com/muhuk/validation-benchmark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.roomkey/annotate "1.0.1"]
                 [criterium "0.4.3"]
                 [org.clojure/clojure "1.8.0"]
                 [com.velisco/herbert "0.7.0-alpha2"]
                 [org.clojure/tools.cli "0.3.3"]
                 [incanter/incanter-core "1.5.6"]
                 [incanter/incanter-charts "1.5.6"]
                 [io.aviso/pretty "0.1.23"]
                 [prismatic/schema "1.0.4"]
                 [table "0.5.0"]]
  :plugins [[io.aviso/pretty "0.1.23"]]
  :main ^:skip-aot validation-benchmark.core
  :jvm-opts []
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
