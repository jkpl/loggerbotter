(ns loggerbotter.t-util
  (:require [loggerbotter.util :as u]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(def values (partial l/channel->seq))

(defn id [_ next] next)

(facts
  "FoldP"
  (fact "FoldP on empty channel is empty"
        (values (u/foldp id (l/channel))) => empty?
        (values (u/foldp id 0 (l/channel))) => empty?)
  (fact "FoldP with default value"
        (values (u/foldp + 1 (l/channel 4 2))) => '(5 7)
        (values (u/foldp id 1 (l/channel 1 3))) => '(1 3))
  (fact "FoldP with no default value"
        (values (u/foldp - (l/channel 6 3 1))) => '(3 2)
        (values (u/foldp id (l/channel 1 3))) => '(3)))
