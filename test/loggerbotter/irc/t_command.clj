(ns loggerbotter.irc.t-command
  (:require [loggerbotter.irc.command :as irc]
            [midje.sweet :refer :all]))

(facts "IRC join command"
       (irc/join "#foo") => "JOIN #foo"
       (irc/join "#foo" "bar") => "JOIN #foo bar")

(facts "IRC names"
       (irc/names "#foo") => "NAMES #foo")

(facts "IRC nick"
       (irc/nick "foobar") => "NICK foobar")

(facts "IRC password"
       (irc/password "secret") => "PASS secret")

(facts "IRC pong"
       (irc/pong "server") => "PONG :server")

(facts "IRC quit"
       (irc/quit "msg") => "QUIT :msg")

(facts "IRC topic"
       (irc/topic "#foo") => "TOPIC #foo"
       (irc/topic "#foo" "bar") => "TOPIC #foo :bar")

(facts "IRC user"
       (irc/user "foo" "bar") => "USER foo 0 * :bar"
       (irc/user "foo" "bar" :usermode 8) => "USER foo 8 * :bar")

(facts "IRC message parser"
       (irc/parse ":server.com 001 param1 param2 :message :body")
           => {:name "server.com" :command "001"
              :parameters ["param1" "param2"]
              :body "message :body"}
       (irc/parse "command :body")
           => {:command "command" :body "body" :parameters []}
       (irc/parse ":foo!bar@server param :body")
           => {:name {:nick "foo" :username "bar" :host "server"}
               :command "param" :body "body" :parameters []}
       (irc/parse "cmd param1 param2 :body")
           => {:command "cmd" :body "body"
               :parameters ["param1" "param2"]})
