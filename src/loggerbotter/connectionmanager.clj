(ns loggerbotter.connectionmanager
  (:require [lamina.core :as l]
            [loggerbotter.util :as util]
            [clj-time.core :as time]))

(defrecord ConnectionManager
           [conf connections connection-factory out-ch close-ch])

(defn new-manager [conf connection-factory]
  (map->ConnectionManager
    {:conf conf
     :connections (atom {})
     :connection-factory connection-factory
     :out-ch (l/channel)
     :close-ch (l/channel)}))

(defn- add-id-and-time [connection-id message]
  {:connection-id connection-id
   :content message
   :time (time/now)})

(defn- connection-closed? [message]
  (= :closed (:message message)))

(defn- start-connection!
  [manager connection-id & {:keys [conf]}]
  (let [factory (:connection-factory manager)
        manager-conf (:conf manager)
        connection-conf (or conf (get-in manager-conf [:servers connection-id]))
        conn (factory manager-conf connection-conf)
        messages (l/map* (partial add-id-and-time connection-id) conn)]
    (l/siphon messages (:out-ch manager))
    (l/siphon (l/filter* connection-closed? messages)
              (:close-ch manager))
    (swap! (:connections manager)
           #(assoc % connection-id conn))))

(defn- get-connection [manager connection-id]
  (-> (:connections manager)
      deref
      (get connection-id)))

(defn- start-all-connections! [manager]
  (doseq [[connection-id conf] (get-in manager [:conf :servers])]
    (start-connection! manager connection-id :conf conf)))

(defn- close-connection! [manager id]
  (let [connections (:connections manager)
        connection (@connections id)]
    (l/close connection)
    (swap! connections #(dissoc % id))))

(defn close! [manager]
  (let [out-ch (:out-ch manager)
        connections (:connections manager)]
    (l/close (:close-ch manager))
    (doseq [[id _] @connections]
      (close-connection! manager id))
    (l/enqueue out-ch :manager-closed)
    (l/close out-ch)))

(defn- delayed-restart! [manager msg]
  (let [timeout (or (:timeout manager) 10000)]
    (util/later
      (fn []
        (start-connection! manager (:id msg)))
      timeout)))

(defn send!
  ([manager connection-id message]
   (l/enqueue (get-connection manager connection-id) message))
  ([manager message]
   (send! manager (:connection-id message) (:message message))))

(defn start! [manager]
  (do (l/receive-all (:close-ch manager)
                     (partial delayed-restart! manager))
      (start-all-connections! manager)
      manager))
