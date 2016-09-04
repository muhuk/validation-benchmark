(defproject validation-benchmark "0.1.0-SNAPSHOT"
  :description "Benchmark for Clojure validation libraries."
  :url "https://github.com/muhuk/validation-benchmark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-time "0.12.0"]
                 [com.roomkey/annotate "1.0.1"]
                 [com.taoensso/truss "1.3.5"]
                 [com.velisco/herbert "0.7.0"]
                 [criterium "0.4.4"]
                 [hiccup "1.0.5"]
                 [incanter/incanter-core "1.5.6"]
                 [incanter/incanter-charts "1.5.6"]
                 [io.aviso/pretty "0.1.26"]
                 [org.clojure/clojure "1.9.0-alpha11"]
                 [org.clojure/tools.cli "0.3.5"]
                 [prismatic/schema "1.1.3"]]
  :plugins [[io.aviso/pretty "0.1.26"]]
  :main ^:skip-aot validation-benchmark.core
  :jvm-opts ^:replace ["-server" "-XX:+AggressiveOpts" "-Djava.awt.headless=true"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
