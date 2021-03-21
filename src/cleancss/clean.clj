(ns cleancss.clean
  (:require
   [cleancss.find        :as find]
   [cleancss.context     :as context]
   [cleancss.compression :as compress]))


(defn by-state
  [state nodes]
  (filter
   (fn [node]
     (case (:type node)

       :selector
       (find/selector? state node)

       :style-rule
       (seq (by-state state (:selectors node)))

       :media-rule
       (seq (by-state state (:rules node)))

       true))
   nodes))


(defn by-context
  [context nodes]
  (filter
   (fn [node]
     (case (:type node)

       :keyframes-rule
       (find/keyframes? context node)

       true))
   nodes))


(defn clean
  [state schema]
  (let [state-schema (by-state state schema)
        context      (context/get-context state-schema)]
    (->> state-schema
         (by-context context)
         (compress/make))))
