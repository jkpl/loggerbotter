(ns loggerbotter.t-meter
  (:require [loggerbotter.meter :as m]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(def values (partial l/channel->seq))

(defn messages [& vs]
  (apply l/channel
         (map-indexed (fn [i v] {:time i :content v})
                      vs)))

(def meter1
  (m/map->ReduceMeter
    {:meter-id    :m1
     :predicate   #(number? (:content %))
     :reducer     #(+ %1 (:content %2))
     :start-value 1}))

(def meter2
  (m/map->ReduceMeter
    {:meter-id    :m2
     :predicate   #((every-pred number? pos?) (:content %))
     :reducer     #(- %1 (:content %2))
     :start-value 20}))

(def meter3
  (m/map->MappingMeter
    {:meter-id  :m3
     :predicate #(string? (:content %))
     :mapper    #(str (:content %) "!")}))

(fact
  "Meter channel"
  (values (m/meter-channel meter1 (messages "x" 3 "foo" 2 1)))
    => '({:meter-id :m1 :value 4 :time 1}
         {:meter-id :m1 :value 6 :time 3}
         {:meter-id :m1 :value 7 :time 4})
  (values (m/meter-channel meter2 (messages 10 -3 3 -1 4)))
    => '({:meter-id :m2 :value 10 :time 0}
         {:meter-id :m2 :value 7 :time 2}
         {:meter-id :m2 :value 3 :time 4})
  (values (m/meter-channel meter3 (messages "foo" 4 2 "bar" 1)))
    => '({:meter-id :m3 :value "foo!" :time 0}
         {:meter-id :m3 :value "bar!" :time 3}))

(fact
  "Meter joining"
  (->> (messages 10 "foo" -4 1 "zap")
       (m/join-meters [meter1 meter2 meter3])
       values
       (group-by :meter-id))
    => {:m1 [{:meter-id :m1 :value 11 :time 0}
             {:meter-id :m1 :value 7 :time 2}
             {:meter-id :m1 :value 8 :time 3}]
        :m2 [{:meter-id :m2 :value 10 :time 0}
             {:meter-id :m2 :value 9 :time 3}]
        :m3 [{:meter-id :m3 :value "foo!" :time 1}
             {:meter-id :m3 :value "zap!" :time 4}]})
