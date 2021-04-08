(ns cleancss.watcher
  (:require
   [cleancss.env    :as env]
   [cleancss.clean  :as clean]
   [clj-ph-css.core :as css]

   [clojure.java.io :as io]
   [clojure.string  :as string]
   [clojure.edn     :as edn]

   [hawk.core                  :as hawk]
   [com.stuartsierra.component :as component])

  (:import
   [java.io File]))


(defn extract-classes
  [^String form]
  (set (re-seq #"(?<=#c/c.*\")(?:.*?)(?=\")" form)))

(defn extract-identifiers
  [^String form]
  (set (re-seq #"(?<=#c/i.*\")(?:.*?)(?=\")" form)))


(defn get-user-file-path
  [^File file]
  (string/replace
   (.getCanonicalPath file)
   (str (System/getProperty "user.dir") File/separator)
   (str)))


(defn make-cache-file-path
  [cache-directory user-file-path]
  (str cache-directory File/separator user-file-path ".edn"))


(defn update-file-cache
  [context ^File file]
  (let [cache-file-path
        (-> (env/get-cache-directory context)
            (make-cache-file-path (get-user-file-path file)))

        file-content
        (slurp file)

        file-selectors
        {:classes     (extract-classes file-content)
         :identifiers (extract-identifiers file-content)}

        new-context
        (assoc-in context [:selectors cache-file-path]
                  file-selectors)]

    (io/make-parents cache-file-path)
    (spit cache-file-path file-selectors)

    new-context))


(defn delete-file-cache
  [context ^File file]
  (let [cache-file-path
        (-> (env/get-cache-directory context)
            (make-cache-file-path (get-user-file-path file)))]

    (io/delete-file (io/file cache-file-path))
    (update context :selectors dissoc cache-file-path)))


(defn get-directory-files
  [directory-path]
  (when (.exists (io/as-file directory-path))
    (->> (io/file directory-path)
         (file-seq)
         (remove #(.isDirectory %)))))


(defn import-css
  [context]
  (some->> (env/build-import-directory context)
           (get-directory-files)
           (filter #(string/ends-with? (.getName %) ".css"))
           (mapcat (comp css/string->schema slurp))))


(defn merge-cache-selectors
  [selectors]
  (apply merge-with into (vals selectors)))




(defn export-css
  [context]
  (->> (:selectors context)
       (merge-cache-selectors)
       (merge-with into (env/get-default-selectors context))
       (clean/clean (:stylesheets context))
       (css/schema->string)
       (spit (env/get-output-file-directory context)))
  context)


(defn get-all-cache
  [context]
  (some->> (env/get-cache-directory context)
           (get-directory-files)
           (reduce
            (fn [cache ^File file]
              (assoc cache (get-user-file-path file)
                     (edn/read-string (slurp file))))
            {})))

(defn watcher-run
  [configuration]
  (let [initialize-context
        {:configuration configuration
         :stylesheets   (import-css configuration)
         :selectors     (get-all-cache configuration)}]

    (hawk/watch!
     [{:paths   (env/get-watch-directory initialize-context)
       :context (constantly initialize-context)
       :handler
       (fn [context event]
         (if (= :delete (:kind event))
           (delete-file-cache context (:file event))
           (-> context
               (update-file-cache (:file event))
               (export-css))))}])))


(defn watcher-stop
  [watcher]
  (hawk/stop! watcher))


(defrecord CleanCssWatcher [configuration]
  component/Lifecycle
  (start [this]
    (if (:watcher this)
      this
      (assoc this :watcher (watcher-run configuration))))
  (stop [this]
    (if (:watcher this)
      (do (watcher-stop (:watcher this))
          (dissoc this :watcher))
      this)))


(defn create
  [configuration]
  (->CleanCssWatcher configuration))

(comment 
  (def s (create {:watch-dirs ["src"]
                  :build {:import {:directory "/home/panthevm/study/cleancss/example/resources/public/css"}
                          :cache  {:directory "ff"}
                          :export {:file "cleancss.css"}}}))
  (.start s)
  (.stop s))

