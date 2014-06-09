(defproject langtons-ant "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2227"]]

  :plugins [[lein-cljsbuild "0.3.4"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "langtons-ant"
              :source-paths ["src"]
              :compiler {:output-to "langtons_ant.js"
                         :output-dir "out"
                         :optimizations :advanced
                         :source-map "langtons_ant.map"}}]})
