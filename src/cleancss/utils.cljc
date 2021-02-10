(ns cleancss.utils
  #?(:clj
     (:require
      [clojure.string       :as string]
      [cleancss.compression :as compress]))
  #?(:cljs (:require-macros [cleancss.utils])))


#?(:clj (defonce classes     (atom {})))
#?(:clj (defonce identifiers (atom {})))
#?(:clj (defonce attributes  (atom {})))

#?(:clj
   (defn escape
     [value]
     (string/replace value #":" "\\\\:")))


#?(:clj
   (defn add-class
     [state name-space class-names]
     (update state name-space
             (fn [ns-state]
               (loop [items  class-names
                      acc    (or ns-state {})]
                 (if (seq items)
                   (let [item (first items)]
                     (if (contains? (apply merge (map second state)) item)
                       (recur (next items) acc)
                       (recur (next items)
                              (assoc acc (first items)
                                     (compress/short-name
                                      (+ (count (mapcat (comp keys second)
                                                        @classes))
                                         (count acc)))))))
                   acc))))))



#?(:clj
   (defn add-identifier
     [ns value]
     (swap! identifiers update ns (fnil conj #{}) value)))


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
       (swap! classes (fn [state] (add-class state ns value)))
       (vec (vals (select-keys (apply merge (map second @classes)) value))))))


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
       (apply add-class (:class value)))
     (->> (dissoc value :id :class)
          (filter
           (some-fn
            (comp boolean? second)
            (comp string? second)))
          (map add-attribute))
     value))
