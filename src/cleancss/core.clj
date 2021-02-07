(ns cleancss.core
  (:gen-class)
  (:require
   [cleancss.import :as import]
   [cleancss.filter :as filter]
   [cleancss.export :as export]))


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
  [options stylesheets]
  (filter/make-clean options stylesheets))


(defn -main [& args])
