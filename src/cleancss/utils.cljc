(ns cleancss.utils
  #?(:clj
     (:require [clojure.string :as string]))
  #?(:cljs
     (:require-macros [cleancss.utils])))

#?(:clj (defonce classes     (atom {})))
#?(:clj (defonce identifiers (atom {})))
#?(:clj (defonce attributes  (atom {})))

#?(:clj
   (defn- add-class
     [ns value]
     (letfn [(escape [v]
               (string/replace v #":" "\\\\:"))]
       (if (sequential? value)
         (swap! classes update ns into (mapv escape value))
         (swap! classes update ns conj (escape value))))))

#?(:clj
   (defn add-identifier
     [ns value]
     (swap! identifiers update ns conj value)))

#?(:clj
   (defn add-attribute
     [ns [attribute-name attribute-value]]
     (swap! attributes update ns (fnil conj #{})
            (cond-> [(name attribute-name)]
              (string? attribute-value)
              (conj attribute-value)))))

#?(:clj
   (defmacro c [& value]
     (let [ns (-> &env :ns :name)]
       (add-class ns value)
       (vec value))))

#?(:clj
   (defmacro i [value]
     (let [ns (-> &env :ns :name)]
       (add-identifier value)
       value)))

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
