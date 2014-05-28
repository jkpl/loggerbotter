(ns loggerbotter.irc.command
  (:require [clojure.string :as string]))

(defn- join-non-blanks [coll]
  (->> coll
       (filter (complement string/blank?))
       (string/join " ")))

(defn- irc-command [command & [params body]]
  (let [param-s (join-non-blanks params)
        body-s (if (string/blank? body) "" (str ":" body))]
    (join-non-blanks [command param-s body-s])))

(defn join [channel & [password]]
  (irc-command "JOIN" (if password
                        [channel password]
                        [channel])))

(defn names [channel]
  (irc-command "NAMES" [channel]))

(defn nick [nickname]
  (irc-command "NICK" [nickname]))

(defn password [pass]
  (irc-command "PASS" [pass]))

(defn pong [server]
  (irc-command "PONG" [] server))

(defn quit [message]
  (irc-command "QUIT" [] message))

(defn topic [channel & [new-topic]]
  (irc-command "TOPIC" [channel] new-topic))

(defn user
  [username realname & {:keys [usermode] :or {usermode 0}}]
  (irc-command "USER" [username (str usermode) "*"] realname))

(defn- parse-name [name]
  (if-let [match (re-find #"(.+)!(.+)@(.+)" name)]
    (->> match
         rest
         (map vector [:nick :username :host])
         (into {}))
    name))

(defn- parse-command [message]
  (let [[command others] (string/split message #" " 2)
        [params-s body] (string/split others #":" 2)
        params (->> (string/split params-s #" ")
                    (filter (complement string/blank?))
                    vec)]
    {:command command
     :parameters params
     :body body}))

(defn- name? [s]
  (= \: (first s)))

(defn parse [message]
  (let [[part1 others] (string/split message #" " 2)]
    (if (name? part1)
      (assoc (parse-command others)
        :name (parse-name (.substring part1 1)))
      (parse-command message))))

(defn ping->pong [message]
  (pong (:body message)))
