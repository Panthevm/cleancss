(ns cleancss.filter
  (:require
   [clojure.string :as string]))


(defn used-selector?
  [used-styles selector]
  (some (partial string/includes? selector)
        used-styles))


(defn remove-unused-stylesheets
  [used-styles node]
  (cond

    (= :style-rule (:type node))
    (when-let [used-selectors
               (seq (filter (partial used-selector? used-styles)
                            (:selectors node)))]
      (assoc node :selectors used-selectors))

    (= :media-rule (:type node))
    (when-let [stylesheets
               (seq (remove-unused-styles used-styles (:rules node)))]
      (assoc node :rules stylesheets))

    (sequential? node)
    (keep (partial remove-unused-styles used-styles)
          node)

    :else node))
