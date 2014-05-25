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
  [& {:keys [host port nick realname channels]}]
  (let [in-ch (tcp-line-client host port)
        out-ch (channel)]
    (siphon (map* irc/parse in-ch) out-ch)
    (on-closed in-ch #(enqueue out-ch :closed))
    (enqueue in-ch (irc/user nick realname))
    (enqueue in-ch (irc/nick nick))
    (doseq [channel channels]
      (enqueue in-ch (irc/join channel)))
    (receive-all (filter* irc/ping? out-ch)
                 #(enqueue in-ch (irc/ping->pong %)))
    [in-ch out-ch]))
