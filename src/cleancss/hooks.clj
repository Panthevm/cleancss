(ns cleancss.hooks
  (:require
   [cleancss.env   :as env]
   [cleancss.data  :as data]
   [cleancss.cache :as cache]
   [cleancss.clean :as clean]

   [clojure.set                   :as set]
   [clojure.java.io               :as io]
   [clojure.tools.namespace.find  :as ns-find]
   [clojure.tools.namespace.file  :as ns-file]
   [clojure.tools.namespace.parse :as ns-parse]))


(defn- get-sources
  [directory]
  (ns-find/find-sources-in-dir
   (io/file directory)
   {:extensions [".clj" ".cljc" ".cljs"]}))


(defn- get-namespace-name
  [source]
  (ns-parse/name-from-ns-decl
   (ns-file/read-file-ns-decl
    source
    {:read-cond :allow
     :features  #{:clj :cljs}})))


(defn reset
  [& _]
  (if-let [context @env/context]
    (let [cache-directory
          (-> context
              env/get-cache-directory)
          
          cache-namespaces
          (-> cache-directory
              cache/get-all-cache
              cache/get-cache-namespaces)

          exists-sources
          (->> context
               env/get-watch-directory
               (mapcat get-sources))

          exists-namespaces
          (map get-namespace-name exists-sources)

          modified-namespace
          (get-namespace-name
           (last (sort-by #(.lastModified %) exists-sources)))

          remove-cache-namespaces
          (conj
           (set/difference
            (set cache-namespaces)
            (set exists-namespaces))
           modified-namespace)]

      (prn exists-namespaces)

      (doseq [namespace-name remove-cache-namespaces]
        (cache/remove-cache-namespace
         (env/get-cache-directory context)
         namespace-name)))

    (env/initialize-context "cleancss.edn")))


(defn build
  [& _]
  (let [context @env/context]
    (-> (env/get-cache-directory context)
        cache/get-all-cache
        cache/get-cache-selectors
        (merge (env/get-default-selectors context))
        (clean/clean (env/get-stylesheets context))
        (data/schema->resource
         (env/get-output-file-directory context)))))
