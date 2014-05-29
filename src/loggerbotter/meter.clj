(ns loggerbotter.meter
  (:require [lamina.core :as l]
            [loggerbotter.util :refer [foldp]]))

(defprotocol Meter
  (meter-id [meter] "Id of the meter")
  (predicate [meter] "Predicate function for meter")
  (channel-mapper
    [meter ch]
    "Produces a channel that maps over the given channels values"))

(defrecord ReduceMeter [id predicate reducer start-value]
  Meter
  (meter-id [meter] (:id meter))
  (predicate [meter] (:predicate meter))
  (channel-mapper [meter ch]
    (foldp (:reducer meter) (:start-value meter) ch)))

(defrecord MappingMeter [id predicate mapper]
  Meter
  (meter-id [meter] (:id meter))
  (predicate [meter] (:predicate meter))
  (channel-mapper [meter ch]
    (l/map* (:mapper meter) ch)))

(defn- wrap-value-with-meter-id [meter v]
  {:id (meter-id meter) :value v})

(defn meter-filter [meter ch]
  (l/filter* (predicate meter) ch))

(defn meter-channel [meter ch]
  (->> ch
       (meter-filter meter)
       (channel-mapper meter)))

(defn- meter-channel-with-id [meter ch]
  (l/map* (partial wrap-value-with-meter-id meter)
          (meter-channel meter ch)))

(defn join-meters [meters ch]
  (->> meters
       (map #(meter-channel-with-id % (l/fork ch)))
       (apply l/merge-channels)))