(ns loggerbotter.irc.predicate)

(defn ping? [message] (= "PING" (:command message)))

(defn join? [message] (= "JOIN" (:command message)))

(defn part? [message] (= "PART" (:command message)))

(defn quit? [message] (= "QUIT" (:command message)))

(defn kick? [message] (= "KICK" (:command message)))

(defn names-reply? [message] (= "353" (:command message)))