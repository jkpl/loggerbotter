(ns loggerbotter.database
  (:require [clj-time [core :as time] [coerce :as time-coerce]]
            [cheshire [generate :refer [add-encoder]]]
            [com.ashafa.clutch :as clutch])
  (:gen-class))

(defprotocol Database
  (save-raw-data! [db data] "Save unprocessed data to database")
  (save-meter-data! [db data] "Save meter data to database")
  (get-meter-data [db after-date] "Fetch all meter data after given date"))

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
