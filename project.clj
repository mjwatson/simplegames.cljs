(defproject simplegames "0.1.0-SNAPSHOT"

  :description "Some simple games"

  :url "https://github.com/mjwatson/simplegames.cljs.git"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [ring "1.2.0"]
                 [compojure "1.1.5"]
                 [enlive "1.1.1"]]

  :plugins [[lein-cljsbuild "0.3.2"]
            [com.cemerick/austin "0.1.0"]
            [lein-ring "0.8.3"]]

  :uberjar-name "simplegames.jar"

  :min-lein-version "2.0.0"

  :hooks [leiningen.cljsbuild]

  :source-paths ["src/clj"]

  :cljsbuild { 
    :builds {
      :main {
        :source-paths ["src/cljs"]
        :compiler {:output-to "resources/public/js/simplegames.js"
                   :optimizations :simple
                   :pretty-print true }
        :jar true}}}

     :ring {:handler simplegames.server/handler }

     :main simplegames.server

   )

