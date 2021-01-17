(ns cleancss.utils
  #?(:cljs
     (:require-macros [cleancss.utils])))

(defonce classes-
  (atom #{}))

(defonce identifiers-
  (atom #{}))

(defonce attributes-
  (atom #{}))

(defn- add-class
  [value]
  (if (sequential? value)
    (swap! classes- update into value)
    (swap! classes- update conj value)))

(defn- add-identifier
  [value]
  (swap! identifiers- update conj value))

(defn- add-attribute
  [[attribute-name attribute-value]]
  (swap! attributes- update conj
         (cond-> [(name attribute-name)]
           (string? attribute-value)
           (conj attribute-value))))

#?(:clj
   (defmacro classes [value]
     (add-class value)
     value)

   (defmacro identifier [value]
     (add-identifier value)
     value)


   (defmacro attribute [value]
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
