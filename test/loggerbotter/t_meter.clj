(ns loggerbotter.t-meter
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
  (let [meter1 (m/->Meter :m1 number? + 0)
        meter2 (m/->Meter :m2 (every-pred number? pos?) - 20)]
    (fact "Meter mapping"
          (values (m/map-meter meter1 (l/channel "x" 3 "foo" 2 1)))
            => '(3 5 6)
          (values (m/map-meter meter2 (l/channel 10 -3 3 -1 4)))
            => '(10 7 3))
    (fact "Meter joining"
          (->> (l/channel 10 "foo" -4 1)
               (m/join-meters [meter1 meter2])
               values
               (group-by :id))
            => {:m1 [{:id :m1 :value 10}
                     {:id :m1 :value 6}
                     {:id :m1 :value 7}]
                :m2 [{:id :m2 :value 10}
                     {:id :m2 :value 9}]})))
