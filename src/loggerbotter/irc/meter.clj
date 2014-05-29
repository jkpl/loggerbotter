(ns loggerbotter.irc.meter
  (:require [loggerbotter.irc [predicate :refer :all]]
            [loggerbotter
             [meter :refer [map->ReduceMeter]]
             [util :refer [in?]]]
            [clojure.string :as string]))

(defn- map-message [f previous message]
  (f previous (:message message)))

(defn- match-for-server [server predicate message]
  (and (= server (:id message))
       (predicate (:message message))))

(defn- channel-nicks-change? [channel message]
  (or (and (in? (:command message) ["JOIN" "PART" "KICK"])
           (= channel (first (:parameters message))))
      (and (names-reply? message)
           (= channel (get (:parameters message) 2)))
      (quit? message)))

(defn- nicks-from-message [message]
  (cond
    ((some-fn join? quit? part?) message) (-> message :name :nick vector)
    (kick? message) (-> (:parameters message) second vector)
    (names-reply? message) (string/split (:body message) #" ")
    :else []))

(defn- collect-nicks [previous-nicks message]
  (let [nicks (nicks-from-message message)
        add-nicks ((some-fn names-reply? join?) message)]
    (into #{}
          (if add-nicks
            (concat previous-nicks nicks)
            (remove (set nicks) previous-nicks)))))

(defn channel-nicks-meter [server channel]
  (map->ReduceMeter
    {:id          (str server "/" channel)
     :start-value []
     :predicate   (partial match-for-server server
                           (partial channel-nicks-change? channel))
     :reducer     (partial map-message collect-nicks)}))