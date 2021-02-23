(ns cleancss.cljs.utils
  (:require
   [clojure.string :as string]))


(defn escape
  [^String value]
  (-> value
      (string/replace #":" "\\\\:")
      (string/replace #"/" "\\\\/")))


(defn add-values
  [state ns-name group values]
  (update-in state [ns-name group]
             (fnil into #{})
             (map escape values)))


(defn get-values
  [state group]
  (into #{}
        (mapcat group)
        (vals state)))


(defn add-attributes
  [state name-space attributes]
  (update-in state [name-space :attributes]
             (fnil into #{})
             (keep (fn [[attribute-name attribute-value]]
                     (when-not (fn? attribute-value)
                       (if (boolean? attribute-value)
                         [(name attribute-name)]
                         [(name attribute-name) attribute-value])))
                   attributes)))
