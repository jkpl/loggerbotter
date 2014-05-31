(defproject loggerbotter "0.1.0-SNAPSHOT"
  :description "bot for logging IRC messages"
  :url "http://github.com/jkpl/loggerbotter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [aleph "0.3.2"]
                 [clj-time "0.6.0"]
                 [potemkin "0.3.4"]
                 [com.ashafa/clutch "0.4.0-RC1"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.8"]]
  :main loggerbotter.core
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
