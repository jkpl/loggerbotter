(ns loggerbotter.database
  (:require [clj-time [core :as time] [coerce :as time-coerce]]
            [cheshire [generate :refer [add-encoder]]]
            [com.ashafa.clutch :as clutch])
  (:gen-class))

(defprotocol Database
  (save-raw-data! [db data] "Save unprocessed data to database")
  (save-meter-data! [db data] "Save meter data to database")
  (get-meter-data [db after-date] "Fetch all meter data after given date"))

(defn- conj-to-key [d ks v]
  (update-in d ks
             (fnil #(conj % v) [])))

(defrecord InMemoryDatabase [db-atom]
  Database
  (save-raw-data! [db data]
    (swap! (:db-atom db)
           #(conj-to-key % [:raw-log] data)))
  (save-meter-data! [db data]
    (swap! (:db-atom db)
           #(conj-to-key % [:meters] data)))
  (get-meter-data [db after-date]
    (->> (deref (:db-atom db))
         :meters
         (filter #(time/before? after-date (:time %)))
         (sort-by :time))))

(defn create-memory-db []
  (->InMemoryDatabase (atom {})))

(defn- remove-data-older-than [coll date]
  (remove #(time/after? date (:time %)) coll))

(defn- remove-old-data-for-field [db field before-date]
  (update-in db [field]
             #(remove-data-older-than % before-date)))

(defn drop-old-data [memory-db before-date]
  (swap! (:db-atom memory-db)
         (fn [db]
           (reduce
             #(remove-old-data-for-field %1 %2 before-date)
             db [:meters :raw-log]))))

(add-encoder org.joda.time.DateTime
             (fn [dt json-generator]
               (.writeString json-generator
                             (str (time-coerce/to-string dt)))))

(defrecord CouchDatabase [db-url]
  Database
  (save-raw-data! [db data]
    (if (map? data)
      (clutch/put-document (:db-url db)
                           (into {:datatype "raw-log"} data))))
  (save-meter-data! [db data]
    (if (map? data)
      (clutch/put-document (:db-url db)
                           (into {:datatype "meter"} data))))
  (get-meter-data [db after-date]
    (->> (clutch/get-view (:db-url db) "meterdata" "by-time"
                          {:startkey (time-coerce/to-string after-date)})
         (map :value))))

(def meterdata-by-time-javascript
  "function(doc, req) {
    if (doc.time && doc.datatype === \"meter\") {
      emit(doc.time, doc);
    }
  }")

(def meterdata-javascript-views
  {:by-time {:map meterdata-by-time-javascript}})

(defn initialize-couchdb-db [db-url]
  (clutch/get-database db-url)
  (clutch/save-view db-url "meterdata"
                    [:javascript meterdata-javascript-views])
  (->CouchDatabase db-url))

(defrecord CachedDatabase [master cache]
  Database
  (save-raw-data! [db data]
    (do (save-raw-data! (:master db) data)
        (save-raw-data! (:cache db) data)))
  (save-meter-data! [db data]
    (do (save-meter-data! (:master db) data)
        (save-meter-data! (:cache db) data)))
  (get-meter-data [db after-date]
    (let [data (get-meter-data (:cache db) after-date)]
      (if (empty? data)
        (get-meter-data (:master db) after-date)
        data))))
