(ns loggerbotter.util)

(defn later [f d]
  (future
    (Thread/sleep d)
    (f)))

(defn apply-kv [f args]
  (apply f (apply concat args)))