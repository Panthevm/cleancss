(ns cleancss.cljs.hooks
  (:require
   [cleancss.core       :as core]
   [cleancss.cljs.env   :as env]
   [cleancss.cljs.state :as state]

   [clojure.java.io   :as io]

   [cljs.util         :as cljs-util]
   [cljs.build.api    :as build-api]
   [cljs.analyzer.api :as analyzer-api]
   [cljs.compiler.api :as compiler-api]))


(defonce initialize
  (build-api/mark-cljs-ns-for-recompile!
   'cleancss.cljs.state "resources/public/js/out"))


(defn reset
  [& _]
  (when-let
      [macro-namespaces
       (seq (build-api/cljs-dependents-for-macro-namespaces
             ['cleancss.cljs.state]))]

    (let [all-app-namespaces
          (->> (:watch-dirs env/config)
               (mapcat (comp compiler-api/cljs-files-in io/file))
               (map
                (fn [file]
                  {:ns (-> file .getPath analyzer-api/parse-ns :ns)
                   :modified (-> file cljs-util/last-modified)})))

          last-modified-namespace
          (when (seq all-app-namespaces)
            (apply max-key :modified all-app-namespaces))]

      (swap! state/state
             (fn [state]
               (-> state
                   (dissoc (:ns last-modified-namespace))
                   (select-keys macro-namespaces)))))))


(defn build-state
  [state]
  (-> state
      (update :identifiers #(if % % (state/get-identifiers)))
      (update :attributes  #(if % % (state/get-attributes)))
      (update :classes     #(if % % (state/get-classes)))
      (update :types       #(if % % env/types))
      (update :pseudos     #(if % % env/pseudos))
      (update :functions   #(if % % env/functions))))


(defn build
  [& _]
  (let [state         (-> env/config :state build-state)
        export-config (-> env/config :build :export)]
    (->> env/stylesheets
         (core/clean state)
         (core/export-to-file export-config))))
