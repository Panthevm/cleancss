(ns cleancss.cache
  (:require
   [clojure.java.io :as io]
   [clojure.edn     :as edn])
  (:import
   [java.io File]))


(defn read-cache
  [source]
  (->> source slurp edn/read-string))


(defn get-all-cache
  [^String output-directory]
  (->> output-directory io/file file-seq rest (map read-cache)))


(defn get-cache-selectors
  [caches]
  (apply merge-with into (map :selectors caches)))


(defn get-cache-namespaces
  [caches]
  (map :namespace caches))


(defn update-namespace-cache
  [output-directory namespace-name selector value]
  (let [cache-path (str output-directory "/" namespace-name ".edn")
        cache      (if (.exists (io/file cache-path))
                     (read-cache cache-path)
                     (do (io/make-parents cache-path) {}))]
    (spit cache-path
          (-> cache
              (update-in [:selectors selector] (fnil into #{}) value)
              (assoc :namespace namespace-name)))))


(defn remove-cache-namespace
  [output-directory namespace-name]
  (let [cache-path (str output-directory "/" namespace-name ".edn")]
    (when (.exists (io/file cache-path))
      (io/delete-file cache-path))))
