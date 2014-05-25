(ns loggerbotter.connectionmanager
  (:require [lamina.core :refer :all]
            [loggerbotter.irc.client :as irc]))

(defn- add-source-id [id message]
  {:id id :message message})

(defn- connection-closed? [message]
  (= :closed (:message message)))

(defn- irc-connection [out-ch id nick realname conf]
  (let [[_ ch] (irc/connect-to-irc
                 :host (:host conf) :port (:port conf)
                 :nick nick :realname realname
                 :channels (:channels conf))
        messages (map* (partial add-source-id id) ch)]
    (siphon messages out-ch)))

(defn- later [f d]
  (future
    (Thread/sleep d)
    (f)))

(defn start-manager
  [conf]
  (let [out-ch (channel)
        nick (:nick conf)
        realname (:realname conf)
        timeout (or (:timeout conf) 10000)
        server-conf #(get-in conf [:servers %])
        connection-for-id (fn [id]
                            (irc-connection out-ch id nick realname
                                            (server-conf id)))]
    (doseq [[id server] (:servers conf)]
      (irc-connection out-ch id nick realname server))
    (receive-all (filter* connection-closed? out-ch)
                 (fn [m] (later #(connection-for-id (:id m)) timeout)))
    out-ch))