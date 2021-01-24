(ns cleancss.hooks
  (:require
   [cleancss.core     :as core]
   [cleancss.utils    :as utils]
   [cleancss.defaults :as defaults]

   [clojure.java.io :as io]
   [clojure.edn     :as edn]
   [cljs.build.api  :as cljs-api])

  (:import
   [java.io PushbackReader]))


(def configuration
  (-> (io/file "cleancss.edn") io/reader PushbackReader. edn/read))


(def stylesheets
  (-> configuration :build :import core/import-from-file))


(defn concat-map-values
  [data]
  (reduce-kv
   (fn [acc k v]
     (into acc v))
   #{} data))


(defn application-update
  [application]
  (-> application
      (update :identifiers #(if (empty? %) (concat-map-values @utils/identifiers) %))
      (update :attributes  #(if (empty? %) (concat-map-values @utils/attributes)  %))
      (update :classes     #(if (empty? %) (concat-map-values @utils/classes)     %))
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
  (swap! utils/identifiers assoc (:ns session) #{})
  (swap! utils/attributes  assoc (:ns session) #{})
  (swap! utils/classes     assoc (:ns session) #{}))
