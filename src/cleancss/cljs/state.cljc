(ns cleancss.cljs.state
  #?(:clj 
     (:require
      [cleancss.cljs.utils :as u]))
  #?(:cljs
     (:require-macros
      [cleancss.cljs.state])))


#?(:clj
   (defonce state (atom {})))


#?(:clj
   (defn get-classes
     []
     (u/get-values @state :classes)))


#?(:clj
   (defn get-identifiers
     []
     (u/get-values @state :identifiers)))


#?(:clj
   (defn get-attributes
     []
     (u/get-values @state :attributes)))


#?(:clj
   (defmacro c [& classes]
     (let [ns' (-> &env :ns :name)]
       (swap! state
              #(u/add-values % ns' :classes classes)))
     (vec classes)))


#?(:clj
   (defmacro i [identifier]
     (let [ns' (-> &env :ns :name)]
       (swap! state
              #(u/add-values % ns' :identifiers [identifier])))
     identifier))


#?(:clj
   (defmacro a [attributes]
     (let [ns'        (-> &env :ns :name)
           identifier (:id attributes)
           classes    (:class attributes)]

       (when identifier
         (swap! state
                #(u/add-values % ns' :identifiers [identifier])))

       (when classes
         (swap! state
                #(u/add-values % ns' :classes classes)))

       (when (seq attributes)
         (swap! state
                #(u/add-attributes % ns' attributes)))

       attributes)))
