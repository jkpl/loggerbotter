(ns loggerbotter.util
  (:require [lamina.core :as l]))

(defn later [f d]
  (future
    (Thread/sleep d)
    (f)))

(defn apply-kv [f args]
  (apply f (apply concat args)))

(defn in? [x seq]
  (some (partial = x) seq))

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

(defn lift [f & chs]
  (l/map* (partial apply f) (l/zip chs)))
