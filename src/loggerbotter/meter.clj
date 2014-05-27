(ns loggerbotter.meter
  (:require [lamina.core :as l]))

(defrecord Meter [namespace predicate mapper])

(defn- namespaced-value [meter v]
  {:namespace (:namespace meter)
   :value v})

(defn- foldp-step [f last-val x]
  (let [previous @last-val]
    (if (= previous :foldp-default-value)
      (do (reset! last-val x) :foldp-default-value)
      (swap! last-val #(f % x)))))

(defn foldp
  ([f v channel]
   (->> channel
        (l/map* (partial foldp-step f (atom v)))
        (l/filter* (partial not= :foldp-default-value))))
  ([f channel]
   (foldp f :foldp-default-value channel)))

(defn map-meter [meter in-ch]
  (->> in-ch
       (l/filter* (:predicate meter))
       (foldp (:mapper meter))))

(defn- map-meter-with-namespace [meter in-ch]
  (l/map* (partial namespaced-value meter)
          (map-meter meter in-ch)))

(defn join-meters [meters in-ch]
  (->> meters
       (map #(map-meter-with-namespace % (l/fork in-ch)))
       (apply l/merge-channels)))