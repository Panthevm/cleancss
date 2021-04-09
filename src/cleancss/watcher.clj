(ns cleancss.watcher
  (:require
   [cleancss.clean  :as clean]

   [clojure.java.io :as io]
   [clojure.edn     :as edn]
   [clojure.string  :as string]

   [clj-ph-css.core :as css]
   [hawk.core       :as hawk])

  (:import
   [java.io File]))


(defn build-import-directory
  [context]
  (-> context :build :import :directory))


(defn get-output-file-directory
  [context]
  (-> context :configuration :build :export :file))


(defn get-cache-directory
  [context]
  (-> context :configuration :build :cache :directory))

(defn get-watch-directory
  [context]
  (-> context :configuration :watch-dirs))


(defn get-default-selectors
  [context]
  (-> context :configuration :default))


(defn extract-classes
  [^String form]
  (set (map last (re-seq #"(?:#c)(\s|,)*?\"(.*?)\"" form))))


(defn extract-identifiers
  [^String form]
  (set (map last (re-seq #"(?:#i)(\s|,)*?\"(.*?)\"" form))))


(defn user-file-path
  [^File file]
  (string/replace
   (.getCanonicalPath file)
   (str (System/getProperty "user.dir") File/separator)
   (str)))


(defn cache-file-path
  [cache-directory user-file-path]
  (str cache-directory File/separator user-file-path ".edn"))


(defn directory-files
  [directory-path]
  (when (.exists (io/as-file directory-path))
    (->> (io/file directory-path)
         (file-seq)
         (remove #(.isDirectory %)))))


(defn content-selectors
  [^String form]
  {:classes     (extract-classes form)
   :identifiers (extract-identifiers form)})


(defn update-file-cache
  [context ^File file]
  (let [cache-file-path
        (->> (user-file-path file)
             (cache-file-path (get-cache-directory context)))

        file-selectors
        (content-selectors (slurp file))

        new-context
        (assoc-in context [:selectors cache-file-path]
                  file-selectors)]

    (io/make-parents cache-file-path)
    (spit cache-file-path file-selectors)

    new-context))


(defn delete-file-cache
  [context ^File file]
  (let [cache-file-path
        (->> (user-file-path file) 
             (cache-file-path (get-cache-directory context)))]

    (io/delete-file (io/file cache-file-path))
    (update context :selectors dissoc cache-file-path)))


(defn import-css
  [context]
  (some->> (build-import-directory context)
           (directory-files)
           (filter #(string/ends-with? (.getName %) ".css"))
           (mapcat (comp css/string->schema slurp))))


(defn merge-cache-selectors
  [selectors]
  (apply merge-with into (vals selectors)))


(defn add-default-selectors
  [context selectors]
  (->> (get-default-selectors context)
       (merge-with into selectors)))


(defn export-css
  [context]
  (->> (:selectors context)
       (merge-cache-selectors)
       (add-default-selectors context)
       (clean/clean (:stylesheets context))
       (css/schema->string)
       (spit (get-output-file-directory context)))
  context)


(defn get-all-cache
  [context]
  (some->> (get-cache-directory context)
           (directory-files)
           (reduce
            (fn [cache ^File file]
              (assoc cache (user-file-path file)
                     (edn/read-string (slurp file))))
            {})))


(defn watcher-run
  [initialize-context]
  (hawk/watch!
   [{:paths   (get-watch-directory initialize-context)
     :context (constantly initialize-context)
     :handler
     (fn [context event]
       (if (= :delete (:kind event))
         (-> context
             (delete-file-cache (:file event))
             (export-css))
         (-> context
             (update-file-cache (:file event))
             (export-css))))}]))


(defn watcher-stop
  [watcher]
  (hawk/stop! watcher))


(defn create
  [configuration]
  (watcher-run
   {:configuration configuration
    :stylesheets   (import-css configuration)
    :selectors     (get-all-cache
                    {:configuration configuration})}))
