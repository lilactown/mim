(defproject mim "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [me.raynes/conch "0.8.0"]
                 [mount "0.1.12"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/tools.logging "0.4.0"]]
  :main ^:skip-aot mim.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
