(ns cleancss.utils
  #?(:cljs
     (:require-macros [cleancss.utils])))

(defonce styles
  (atom #{}))

#?(:clj
   (defmacro c [names]
     (swap! styles into (map name names))))
