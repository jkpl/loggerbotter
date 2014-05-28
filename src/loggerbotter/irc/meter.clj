(ns loggerbotter.irc.meter
  (:require [loggerbotter.irc [predicate :refer :all]]
            [loggerbotter.meter :refer [map->Meter]]))

(defn in? [x seq]
  (some (partial = x) seq))

(defn channel-user-change? [channel message]
  (or (and ((some-fn join? part? kick?) message)
           (= channel (first (:parameters message))))
      (quit? message)))

(defn- match-for-server [server predicate message]
  (and (= server (:id message))
       (predicate (:message message))))

(defn- user-from-message [message]
  (cond
    (join? message) (:name message)
    ((some-fn quit? part?) message) (-> message :name :nick)
    (kick? message) (second (:parameters message))))

(defn collect-user [users message]
  (let [user (user-from-message message)]
    (if (join? message)
      (conj users user)
      (remove #(= user (:nick %)) users))))

(defn- map-message [f previous message]
  (f previous (:message message)))

(defn channel-users-meter [server channel]
  (map->Meter {:id        (str server "/" channel)
               :start-value []
               :predicate (partial match-for-server server
                                   (partial channel-user-change? channel))
               :mapper    (partial map-message collect-user)}))