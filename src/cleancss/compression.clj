(ns cleancss.compression
  (:require
   [clojure.string :as string]))

(def symbols        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
(def symbols-length (count symbols))

(defn short-name
  [number]
  (loop [iterator number
         result   nil]
    (let [remainder (mod iterator symbols-length)]
      (cond
        (and (zero? iterator) (not result))
        (str (nth symbols remainder))

        (zero? iterator)
        result

        :else
        (recur (/ (- iterator remainder) symbols-length)
               (str result (nth symbols remainder)))))))
