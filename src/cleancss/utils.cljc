(ns cleancss.utils
  #?(:cljs
     (:require-macros [cleancss.utils])))


(defonce selectors
  (atom
   {:types       #{}
    :classes     #{}
    :pseudos     #{}
    :functions   #{}
    :namespaces  #{}
    :attributes  #{}
    :identifiers #{}}))

(defn- add-class
  [value]
  (if (sequential? value)
    (swap! selectors update :classes into value)
    (swap! selectors update :classes conj value)))

(defn- add-identifier
  [value]
  (swap! selectors update :identifiers conj value))

(defn- add-attribute
  [[attribute-name attribute-value]]
  (swap! selectors update :attributes conj
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
