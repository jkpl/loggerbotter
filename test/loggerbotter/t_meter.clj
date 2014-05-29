(ns loggerbotter.t-meter
  (:require [loggerbotter.meter :as m]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(def values (partial l/channel->seq))

(let [meter1 (m/->ReduceMeter :m1 number? + 0)
      meter2 (m/->ReduceMeter :m2 (every-pred number? pos?) - 20)
      meter3 (m/->MappingMeter :m3 string? #(str % "!"))]
  (fact "Meter channel"
        (values (m/meter-channel meter1 (l/channel "x" 3 "foo" 2 1)))
          => '(3 5 6)
        (values (m/meter-channel meter2 (l/channel 10 -3 3 -1 4)))
          => '(10 7 3)
        (values (m/meter-channel meter3 (l/channel 3 4 "foo" 2 "bar")))
          => '("foo!" "bar!"))
  (fact "Meter joining"
        (->> (l/channel 10 "foo" -4 1 "zap")
             (m/join-meters [meter1 meter2 meter3])
             values
             (group-by :id))
          => {:m1 [{:id :m1 :value 10}
                   {:id :m1 :value 6}
                   {:id :m1 :value 7}]
              :m2 [{:id :m2 :value 10}
                   {:id :m2 :value 9}]
              :m3 [{:id :m3 :value "foo!"}
                   {:id :m3 :value "zap!"}]}))
