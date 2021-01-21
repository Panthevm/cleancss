(ns cleancss.hooks
  (:require
   [cleancss.core     :as core]
   [cleancss.utils    :as utils]
   [cleancss.defaults :as defaults]

   [clojure.java.io :as io]
   [clojure.edn     :as edn])

  (:import
   [java.io PushbackReader]))


(def configuration
  (-> (io/file "cleancss.edn") io/reader PushbackReader. edn/read))


(def stylesheets
  (-> configuration :build :import core/import-from-file))


(defn application-update
  [application]
  (-> application
      (update :identifiers #(if (empty? %) @utils/identifiers- %))
      (update :attributes  #(if (empty? %) @utils/attributes-  %))
      (update :classes     #(if (empty? %) @utils/classes-     %))
      (update :types       #(if (= :all %) defaults/types      %))
      (update :pseudos     #(if (= :all %) defaults/pseudos    %))
      (update :functions   #(if (= :all %) defaults/functions  %))))


(defn build
  [session]
  (->> stylesheets
       (core/clean          (-> configuration :application application-update))
       (core/export-to-file (-> configuration :build :export))))


(defn reset
  [session]
  (reset! utils/identifiers- #{})
  (reset! utils/attributes-  #{})
  (reset! utils/classes-     #{}))
