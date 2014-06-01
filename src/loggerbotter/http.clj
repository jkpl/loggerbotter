(ns loggerbotter.http
  (:require [loggerbotter.database :as db]
            [clj-time.core :as time]
            [lamina.core :as l]
            [org.httpkit.server :as hkit]
            [cheshire.core :as json]
            [ring.util.response :as response]
            [compojure
             [core :refer :all]
             [route :as route]]))

(defn- get-meter-data [database]
  (db/get-meter-data database (time/minus (time/now) (time/days 7))))

(defn- create-socket-handler [database meter-ch]
  (fn [request]
    (hkit/with-channel
      request ch
      (doseq [msg (map json/generate-string (get-meter-data database))]
        (hkit/send! ch msg))
      (l/receive-all meter-ch
                     #(hkit/send! ch (json/generate-string %))))))

(defn- app-routes [socket-handler]
  (routes
    (GET ["/"] {} (response/resource-response "index.html" {:root "public"}))
    (GET ["/ws"] {} socket-handler)
    (route/resources "/")
    (route/not-found "Page not found")))

(defn start-server [conf database meter-ch]
  (hkit/run-server (app-routes (create-socket-handler database meter-ch))
                   (:http conf)))

(defn stop-server [server]
  (server :timeout 100))