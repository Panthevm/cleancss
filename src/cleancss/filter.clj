(ns cleancss.filter
  (:require
   [cleancss.find :as find]))



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
