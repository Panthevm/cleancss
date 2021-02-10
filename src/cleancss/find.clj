(ns cleancss.find
  (:refer-clojure :exclude [class?])
  (:require
   [clojure.string :as string]))


(defn class?
  [application member]
  (contains? (:classes application) (:name member)))


(defn type?
  [application member]
  (contains? (:types application) (:name member)))


(defn pseudo?
  [application member]
  (contains? (:pseudos application) (:name member)))


(defn identifier?
  [application member]
  (contains? (:identifiers application) (:name member)))


(defn function?
  [application member]
  (contains? (:functions application) (:function member)))


(defn used-variable?
  [context declaration]
  (contains? (:used-variables context) (:property declaration)))

(defn variable?
  [context variable]
  (contains? (:variables context) variable))


(defn attribute?
  [application member]
  (some (fn [[attribute-name attribute-value]]
          (when (= attribute-name (:name member))
            (condp = (-> member :operator :name)
              "="  (= attribute-value (:attribute member))
              "^=" (string/starts-with? attribute-value (:attribute member))
              "$=" (string/ends-with?   attribute-value (:attribute member))
              "*=" (string/includes?    attribute-value (:attribute member))
              "~=" (some #(= (:attribute member) %)
                         (string/split attribute-value #" "))
              "|=" (or (= attribute-value (:attribute member))
                       (string/starts-with? attribute-value (str (:attribute member) "-")))
              true)))
        (:attributes application)))


