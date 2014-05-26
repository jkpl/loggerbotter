(ns loggerbotter.core
  (:require [loggerbotter.irc.client :as irc]
            [loggerbotter [connectionmanager :as m] [util :as u]]
            [lamina.core :as l]))

(def conf
  {:nick "loggerbotter"
   :realname "Logger botter"
   :servers {:freenode {:host "irc.freenode.org" :port 6667
                        :channels ["#loggerbotter"]}}})

(defn irc-connection-factory [conf server]
  (->> server
       (merge conf)
       (u/apply-kv irc/irc-client!)))

(defn -main []
  (let [manager (m/new-manager conf irc-connection-factory)]
    (l/receive-all (:out-ch manager) #(println %))
    (m/start! manager)
    manager))