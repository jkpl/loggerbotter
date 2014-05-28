(ns loggerbotter.irc.t-meter
  (:require [loggerbotter.meter :as m]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(def values (partial l/channel->seq))

(defn id [_ next] next)

(facts
  "FoldP"
  (fact "FoldP on empty channel is empty"
        (values (m/foldp id (l/channel))) => empty?
        (values (m/foldp id 0 (l/channel))) => empty?)
  (fact "FoldP with default value"
        (values (m/foldp + 1 (l/channel 4 2))) => '(5 7)
        (values (m/foldp id 1 (l/channel 1 3))) => '(1 3))
  (fact "FoldP with no default value"
        (values (m/foldp - (l/channel 6 3 1))) => '(3 2)
        (values (m/foldp id (l/channel 1 3))) => '(3)))

(facts
  "Meter"
  (let [meter1 (m/->Meter :m1 number? +)
        meter2 (m/->Meter :m2 (every-pred number? pos?) -)]
    (fact "Meter mapping"
          (values (m/map-meter meter1 (l/channel 3 "foo" 2 1)))
            => '(5 6)
          (values (m/map-meter meter2 (l/channel 10 -3 3 -1 4)))
            => '(7 3))
    (fact "Meter joining"
          (->> (l/channel 10 "foo" -4 1)
               (m/join-meters [meter1 meter2])
               values
               (group-by :namespace))
            => {:m1 [{:namespace :m1 :value 6}
                     {:namespace :m1 :value 7}]
                :m2 [{:namespace :m2 :value 9}]})))
