(ns cleancss.hooks
  (:require
   [cleancss.core  :as core]
   [cleancss.state :as state]

   [clojure.java.io :as io]
   [clojure.edn     :as edn]

   [cljs.compiler :as cljs-compler]
   [cljs.util     :as cljs-util]
   [cljs.analyzer.api :as cljs-analyzer]
   [cljs.repl     :as r])

  (:import
   [java.io PushbackReader]))


(def configuration
  (let [cfg (io/file "cleancss.edn")]
    (when (.exists cfg)
      (-> cfg io/reader PushbackReader. edn/read))))


(def stylesheets
  (-> configuration :build :import core/import-from-file))


(defn application-update
  [application]
  (-> application
      (update :identifiers #(if % % (state/get-identifiers @state/state)))
      (update :attributes  #(if % % (state/get-attributes  @state/state)))
      (update :classes     #(if % % (state/get-classes     @state/state)))
      (update :types       #(if (= :all %) state/types      %))
      (update :pseudos     #(if (= :all %) state/pseudos    %))
      (update :functions   #(if (= :all %) state/functions  %))))


(defn build
  [_]
  (->> stylesheets
       (core/clean          (-> configuration :application application-update))
       (core/export-to-file (-> configuration :build :export))))


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

    (swap! state/state
           (fn [state]
             (-> state
                 (dissoc state updated-namespace)
                 (select-keys exists-namespaces))))))
