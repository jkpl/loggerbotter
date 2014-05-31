(ns loggerbotter.irc.core
  (:require [potemkin]
            [loggerbotter.irc
             [client :as client]
             [meter :as meter]]))

(potemkin/import-fn client/irc-client!)
(potemkin/import-fn meter/meters-from-configuration)
