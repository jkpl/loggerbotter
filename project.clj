(defproject loggerbotter "0.1.0-SNAPSHOT"
  :description "bot for logging IRC messages"
  :url "http://github.com/jkpl/loggerbotter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [aleph "0.3.2"]
                 [clj-time "0.6.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
