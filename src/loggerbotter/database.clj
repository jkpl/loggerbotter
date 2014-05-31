(ns loggerbotter.database
  (:require [clj-time.core :as time]))

(defprotocol Database
  (save-raw-data [db data] "Save unprocessed data to database")
  (save-meter-data [db data] "Save meter data to database")
  (get-meter-data [db after-date] "Fetch all meter data after given date"))

(defn- conj-to-key [d ks v]
  (update-in d ks
             (fnil #(conj % v) [])))

(defrecord InMemoryDatabase [db-atom]
  Database
  (save-raw-data [db data]
    (swap! (:db-atom db)
           #(conj-to-key % [:raw-log] data)))
  (save-meter-data [db data]
    (swap! (:db-atom db)
           #(conj-to-key % [:meters] data)))
  (get-meter-data [db after-date]
    (->> (deref (:db-atom db))
         :meters
         (filter #(time/before? after-date (:time %)))
         (sort-by :time))))

(defn new-memory-db []
  (->InMemoryDatabase (atom {})))
