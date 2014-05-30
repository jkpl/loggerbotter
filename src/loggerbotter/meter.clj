(ns loggerbotter.meter
  (:require [lamina.core :as l]
            [loggerbotter.util :refer [foldp lift]]))

(defprotocol Meter
  (meter-id [meter] "Id of the meter")
  (predicate [meter] "Predicate function for meter")
  (channel-mapper
    [meter ch]
    "Produces a channel that maps over the given channels values.
    Each produced value is a map that consist of the meter's ID,
    timestamp from the original message, and the value produced by the meter"))

(defn- meter-frame [meter message value]
  {:meter-id (meter-id meter)
   :value value
   :time (:time message)})

(defrecord ReduceMeter [meter-id predicate reducer start-value]
  Meter
  (meter-id [meter] (:meter-id meter))
  (predicate [meter] (:predicate meter))
  (channel-mapper [meter ch]
    (->> (foldp (:reducer meter) (:start-value meter) (l/fork ch))
         (lift (partial meter-frame meter) ch))))

(defrecord MappingMeter [meter-id predicate mapper]
  Meter
  (meter-id [meter] (:meter-id meter))
  (predicate [meter] (:predicate meter))
  (channel-mapper [meter ch]
    (l/map* (fn [message]
              (meter-frame meter message
                           ((:mapper meter) message)))
            ch)))

(defn meter-filter [meter ch]
  (l/filter* (predicate meter) ch))

(defn meter-channel [meter ch]
  (->> ch
       (meter-filter meter)
       (channel-mapper meter)))

(defn join-meters [meters ch]
  (->> meters
       (map #(meter-channel % (l/fork ch)))
       (apply l/merge-channels)))