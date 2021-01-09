(ns cleancss.core
  (:require
   [cleancss.import :as import]
   [cleancss.export :as export]))


(defn import-from-string
  [stylesheet]
  (import/from-string stylesheet))


(defn import-from-file
  [options]
  (import/from-file options))


(defn clean
  [schema options]
  )


(defn export-to-string
  [schema]
  (export/to-string schema))
