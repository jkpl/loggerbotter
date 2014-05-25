(ns loggerbotter.irc.client
  (:require [lamina.core :refer :all]
            [aleph.tcp :refer [tcp-client]]
            [gloss.core :refer [string]]
            [loggerbotter.irc.command :as irc]))

(defn- tcp-line-client [host port]
  (wait-for-result
    (tcp-client {:host host :port port
                 :frame (string :utf-8 :delimiters ["\r\n"])})))

(defn connect-to-irc
  [out-ch & {:keys [host port nick realname channels]}]
  (let [ch (tcp-line-client host port)
        messages (map* irc/parse ch)
        pings (filter* irc/ping? messages)]
    (on-closed ch #(enqueue out-ch :closed))
    (siphon messages out-ch)
    (enqueue ch (irc/user nick realname))
    (enqueue ch (irc/nick nick))
    (doseq [channel channels]
      (enqueue ch (irc/join channel)))
    (receive-all pings
                 #(enqueue ch (irc/ping->pong %)))
    ch))
