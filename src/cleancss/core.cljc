(ns cleancss.core
  #?(:clj
     (:require
      [cleancss.env   :as env]
      [cleancss.cache :as cache]))
  #?(:cljs
     (:require-macros
      [cleancss.core])))


#?(:clj
   (defmacro c
     "Storing class names"
     [& classes]
     (when @env/context
       (cache/update-namespace-cache
        (-> @env/context env/get-cache-directory)
        (or (-> &env :ns :name) (ns-name *ns*))
        :classes
        classes))
     (vec classes)))


#?(:clj
   (defmacro i
     "Storing idenifier names"
     [idenifier]
     (when @env/context
       (cache/update-namespace-cache
        (-> @env/context env/get-cache-directory)
        (or (-> &env :ns :name)
            (ns-name *ns*))
        :identifiers
        [idenifier]))
     idenifier))


#?(:clj
   (defmacro a
     "Storing attributes"
     [attributes]
     (when @env/context
       (let [attributes-values
             (keep (fn [[attribute-name attribute-value]]
                     (cond
                       (string? attribute-value)
                       [(name attribute-name) attribute-value]

                       (boolean? attribute-value)
                       [(name attribute-name)]))
                   attributes)]

         (cache/update-namespace-cache
          (-> @env/context env/get-cache-directory)
          (or (-> &env :ns :name)
              (ns-name *ns*))
          :attributes
          attributes-values)))
     attributes))
