(ns loggerbotter.irc.t-meter
  (:require [midje.sweet :refer :all]
            [loggerbotter.irc.meter :as m]))

(defn- message [& [id command parameters nick body]]
  {:connection-id id
   :content {:command command
             :parameters parameters
             :name {:nick nick}
             :body body}
   :time :today})

(facts
  "Channel nick meter"
  (let [meter (m/channel-nicks-meter "server" "#loggerbotter")
        p (:predicate meter)
        r (:reducer meter)]
    (fact
      "Predicate matches all channel people changes"
      (p (message "not-server")) => false
      (p (message "server")) => false
      (p (message "server" "JOIN" ["#differentchannel"])) => false
      (p (message "server" "PART" ["#differentchannel"])) => false
      (p (message "server" "KICK" ["#differentchannel"])) => false
      (p (message "server" "353" ["foo" "@" "#differentchannel"])) => false
      (p (message "not-server" "KICK" ["#loggerbotter"])) => false
      (p (message "server" "JOIN" ["#loggerbotter"])) => true
      (p (message "server" "PART" ["#loggerbotter"])) => true
      (p (message "server" "KICK" ["#loggerbotter"])) => true
      (p (message "server" "353" ["foo" "@" "#loggerbotter"])) => true
      (p (message "server" "QUIT")) => true)
    (fact
      "Reducer collects nicks"
      (r #{"peter"} (message "" "JOIN" [] "hans")) => (just "hans" "peter")
      (r #{"klaus" "stefan"} (message "" "PART" [] "klaus")) => (just "stefan")
      (r #{"kurt"} (message "" "QUIT" [] "kurt")) => #{}
      (r #{"henrik"} (message "" "KICK" ["#chan" "henrik"])) => #{}
      (r #{"kirk"} (message "" "353" [] "" "kirk spock bones chekov"))
        => (just "kirk" "spock" "bones" "chekov"))))

(facts
  "Contains text meter"
  (let [meter (m/contains-text-meter "foobar")
        p (:predicate meter)
        f (:mapper meter)]
    (fact
      "Predicate matches only private messages containing text"
      (p (message "" "JOIN" [] "" "foobar")) => false
      (p (message "" "PRIVMSG" [] "" "barfoo")) => false
      (p (message "" "PRIVMSG" [] "" "xxxfoobaryyy")) => true)
    (fact
      "Mapper extracts chat message"
      (f (message "server" "" ["#chan"] "hans" "the message"))
        => {:sender {:nick "hans"} :server "server"
            :target "#chan" :text "the message"})))