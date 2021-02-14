(ns cleancss.core
  (:gen-class)
  (:require
   [cleancss.import      :as import]
   [cleancss.filter      :as filter]
   [cleancss.context     :as context]
   [cleancss.compression :as compress]
   [cleancss.export      :as export]))


(defn import-from-string
  [stylesheet]
  (import/from-string stylesheet))


(defn import-from-file
  [options]
  (import/from-file options))


(defn export-to-string
  [stylesheets]
  (export/to-string stylesheets))


(defn export-to-file
  [options stylesheets]
  (export/to-file options stylesheets))


(defn clean
  [state stylesheets]
  (let [state-stylesheets (filter/clean-by-state state stylesheets)
        context           (context/get-context state-stylesheets)]
    (->> state-stylesheets
         (filter/clean-by-context context)
         (compress/make state context))))


(defn -main [& args])
