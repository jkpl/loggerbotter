(ns loggerbotter.core
  (:require [loggerbotter.irc.core :as irc]
            [loggerbotter
             [connectionmanager :as cm]
             [util :as u]
             [database :as db]
             [meter :as meter]
             [http :as http]]
            [lamina.core :as l]
            [cheshire.core :as json]))

(defonce my-app (atom {}))

(defn irc-connection-factory [conf server]
  (->> server
       (merge conf)
       (u/apply-kv irc/irc-client!)))

(defn irc-connection-manager [conf]
  (cm/new-manager conf irc-connection-factory))

(defn- read-conf-file [conf-file]
  (-> (or conf-file "loggerbotter_config.json")
      slurp
      (json/parse-string keyword)))

(defn- setup-database [conf]
  (db/->CachedDatabase
    (db/initialize-couchdb-db
      (get conf :couchdburl "loggerbotter"))
    (db/create-memory-db)))

(defn start-loggerbotter! [conf]
  (let [meters (irc/meters-from-configuration conf)
        database (setup-database conf)
        manager (irc-connection-manager conf)
        conn-ch (:out-ch manager)
        meter-ch (meter/join-meters meters conn-ch)
        http-server (http/start-server conf database meter-ch)]
    (l/receive-all meter-ch (partial db/save-meter-data! database))
    (l/receive-all conn-ch (partial db/save-raw-data! database))
    (cm/start! manager)
    (reset! my-app {:manager manager :http-server http-server})))

(defn stop-loggerbotter! []
  (when-let [app @my-app]
    (http/stop-server (:http-server app))
    (cm/close! (:manager app))
    (reset! my-app nil)))

(defn -main [& [conf-file]]
  (start-loggerbotter! (read-conf-file conf-file)))