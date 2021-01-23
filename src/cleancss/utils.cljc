(ns cleancss.utils
  #?(:clj
     (:require [clojure.string :as string]))
  #?(:cljs
     (:require-macros [cleancss.utils])))

#?(:clj (defonce classes-     (atom #{})))
#?(:clj (defonce identifiers- (atom #{})))
#?(:clj (defonce attributes-  (atom #{})))

#?(:clj
   (defn- add-class
     [value]
     (letfn [(escape [v]
               (string/replace v #":" "\\\\:"))]
       (if (sequential? value)
         (swap! classes- into (mapv escape value))
         (swap! classes- conj (escape value))))))

#?(:clj
   (defn add-identifier
     [value]
     (swap! identifiers- update conj value)))

#?(:clj
   (defn add-attribute
     [[attribute-name attribute-value]]
     (swap! attributes- update conj
            (cond-> [(name attribute-name)]
              (string? attribute-value)
              (conj attribute-value)))))

#?(:clj
   (defmacro c [value]
     (add-class value)
     value))

#?(:clj
   (defmacro i [value]
     (add-identifier value)
     value))

#?(:clj
   (defmacro a [value]
     (when (:id value)
       (add-identifier (:id value)))
     (when (:class value)
       (add-class (:class value)))
     (->> (dissoc value :id :class)
          (filter
           (some-fn
            (comp boolean? second)
            (comp string? second)))
          (map add-attribute))
     value))
