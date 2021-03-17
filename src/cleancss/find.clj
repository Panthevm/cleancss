(ns cleancss.find
  (:refer-clojure :exclude [class?])
  (:require
   [clojure.string :as string]))


(defn attribute?
  [state member]
  (some
   (fn [[name' value]]
     (when (= name' (:name member))
       (let [attribute (:attribute member)]
         (case (-> member :operator :name)
           "="  (= value (:attribute member))
           "^=" (string/starts-with? value attribute)
           "$=" (string/ends-with?   value attribute)
           "*=" (string/includes?    value attribute)
           "~=" (some #(= attribute %) (string/split value #" "))
           "|=" (or (= value attribute)
                    (string/starts-with? value (str attribute "-")))
           true))))
   (:attributes state)))


(defn selector?
  [state selector]
  (every?
   (fn [member]
     (case (:type member)

       :member
       (let [value (:value member)] 
         (case (:group member)
           :type       (contains? (:types       state) value)
           :class      (contains? (:classes     state) (subs value 1))
           :identifier (contains? (:identifiers state) (subs value 1))
           :pseudo     (contains? (:pseudos     state)
                                  (first (string/split value #"\(")))
           true))

       :member-function
       (contains? (:functions state)
                  (apply str (butlast (:name member))))
       

       :selector-attribute
       (attribute? state member)

       true))
   (:members selector)))


(defn keyframes?
  [context keyframes]
  (contains? (:animations context) (:name keyframes)))


(defn variable?
  [context variable]
  (contains? (:variables context) variable))
