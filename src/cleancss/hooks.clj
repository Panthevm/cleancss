(ns cleancss.hooks
  (:require
   [cleancss.core     :as core]
   [cleancss.utils    :as utils]
   [cleancss.defaults :as defaults]

   [clojure.java.io :as io]
   [clojure.edn     :as edn]

   [cljs.compiler :as cljs-compler]
   [cljs.util     :as cljs-util]
   [cljs.analyzer.api :as cljs-analyzer]
   [cljs.repl     :as r])

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
  [_]
  (prn @utils/classes)
  (->> stylesheets
       (core/clean          (-> configuration :application application-update))
       (core/export-to-file (-> configuration :build :export))))


(defn update-state
  [updated-namespace exists-namespaces state]
  (-> state
      (dissoc updated-namespace)
      (select-keys exists-namespaces)))


(defn reset
  [_]
  (let [namespaces-info
        (->> configuration :watch-dirs
             (mapcat (comp cljs-compler/cljs-files-in io/file))
             (map (fn [file]
                    {:ns       (-> file .getPath cljs-analyzer/parse-ns :ns)
                     :modified (-> file cljs-util/last-modified)})))

        exists-namespaces
        (map :ns namespaces-info)

        updated-namespace
        (->> namespaces-info
             (apply max-key :modified)
             :ns)]

    (prn updated-namespace)
    (swap! utils/identifiers (partial update-state updated-namespace exists-namespaces))
    (swap! utils/attributes  (partial update-state updated-namespace exists-namespaces))
    (swap! utils/classes     (partial update-state updated-namespace exists-namespaces))))
