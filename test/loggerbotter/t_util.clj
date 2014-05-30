(ns loggerbotter.t-util
  (:require [loggerbotter.util :as u]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(def values (partial l/channel->seq))

(defn snd [_ x] x)

(defn id [x] x)

(facts
  "FoldP"
  (fact "FoldP on empty channel is empty"
        (values (u/foldp snd (l/channel))) => empty?
        (values (u/foldp snd 0 (l/channel))) => empty?)
  (fact "FoldP with default value"
        (values (u/foldp + 1 (l/channel 4 2))) => '(5 7)
        (values (u/foldp snd 1 (l/channel 1 3))) => '(1 3))
  (fact "FoldP with no default value"
        (values (u/foldp - (l/channel 6 3 1))) => '(3 2)
        (values (u/foldp snd (l/channel 1 3))) => '(3)))

(facts
  "Lift"
  (fact "Lifting with identity function produces same values"
        (values (u/lift id (l/channel))) => empty?
        (values (u/lift id (l/channel 3 2))) => '(3 2))
  (fact "Lifting with one channel works like map*"
        (values (u/lift inc (l/channel 3 5 6))) => '(4 6 7)
        (values (u/lift dec (l/channel 1 4 2))) => '(0 3 1))
  (fact "Lifting with multiple channels"
        (values (u/lift +
                        (l/channel 1 2 3)
                        (l/channel 4 5 6))) => '(5 7 9)
        (values (u/lift -
                        (l/channel 9 12 5)
                        (l/channel 4 5 6))) => '(5 7 -1)))