(ns cleancss.core
  (:gen-class)
  (:require
   [cleancss.data        :as data]
   [cleancss.filter      :as filter]
   [cleancss.context     :as context]
   [cleancss.compression :as compress]))


(defn string->schema
  [^String value]
  (data/string->schema value))


(defn schema->string
  [schema]
  (data/schema->string schema))


(defn resource->schema
  [resource]
  (data/resource->schema resource))


(defn schema->resource
  [resource schema]
  (data/schema->resource resource schema))


(defn clean
  [state schema]
  (let [state-schema (filter/by-state state schema)
        context      (context/get-context state-schema)]
    (->> state-schema
         (filter/by-context context)
         (compress/make))))
