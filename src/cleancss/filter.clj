(ns cleancss.filter
  (:require
   [clojure.string :as string]))


(defn used-namespace?
  [namespaces member]
  (let [namespace-name (->> member :value drop-last (apply str))]
    (or (string/blank? namespace-name)
        (contains? namespaces namespace-name))))


(defn used-attribute?
  [attributes member]
  (let [operator (-> member :operator :name)]
    (some (fn [[attribute-name attribute-value]]
            (when (= attribute-name (:name member))
              (cond
                (= "=" operator)
                (= attribute-value (:attribute member))

                (= "~" operator)
                (let [values (string/split attribute-value #" ")]
                  (some #{(:attribute member)} values ))

                (= "|" operator)
                (or (= attribute-value (:attribute member))
                    (string/starts-with? attribute-value
                                         (str (:attribute member) "-")))

                (= "^" operator)
                (string/starts-with? attribute-value (:attribute member))

                (= "$" operator)
                (string/ends-with? attribute-value (:attribute member))

                (= "*" operator)
                (string/includes? attribute-value (:attribute member))

                :else true)))
          attributes)))

(declare used-selector?)

(defn used-member?
  [application member]
  (cond
    (= :selector-simple-member (:type member))
    (cond
      (string/starts-with? (:value member) ".")
      (contains? (:classes application) (subs (:value member) 1))

      (string/starts-with? (:value member) ":")
      (some (partial string/starts-with? (:value member))
            (:pseudos application))

      (string/starts-with? (:value member) "#")
      (contains? (:identifiers application) (subs (:value member) 1))

      (string/ends-with? (:value member) "|")
      (or (= "|" (:value member))
          (used-namespace? (:namespaces application) member))

      :else
      (contains? (:types application) (:value member)))

    (= :selector-attribute (:type member))
    (used-attribute? (:attributes application) member)

    (= :selector-member-function (:type member))
    (some (partial string/starts-with? (:name member))
          (:functions application))

    (= :selector-combinator (:type member))
    true

    (= :selector-member-not (:type member))
    (every? (partial used-selector? application)
            (:selectors member))))


(defn used-selector?
  [application selector]
  (every? (partial used-member? application)
          (:members selector)))


(defmulti clean
  (fn [options stylesheet]
    (:type stylesheet)))


(defmethod clean :style-rule
  [options stylesheet]
  (let [used-selectors
        (filter (partial used-selector? (:selectors options))
                (:selectors stylesheet))]
    (when (seq used-selectors)
      (assoc stylesheet :selectors used-selectors))))


(defmethod clean :media-rule
  [options stylesheet]
  (let [stylesheets
        (keep (partial clean options)
              (:rules stylesheet))]
    (when (seq stylesheets)
      (assoc stylesheet :rules stylesheets))))


(defmethod clean :default
  [options stylesheet]
  stylesheet)

(defn make-clean
  [options stylesheets]
  (keep (partial clean options)
        stylesheets))



