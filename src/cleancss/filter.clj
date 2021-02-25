(ns cleancss.filter
  (:require
   [cleancss.find :as find]))


(declare by-state)


(defn used-member?
  [state member]
  (case (:type member)
    :selector-simple-member
    (case (:group member)
      :class      (find/class?      state member)
      :type       (find/type?       state member)
      :pseudo     (find/pseudo?     state member)
      :identifier (find/identifier? state member)
      true)
    :selector-attribute       (find/attribute? state member)
    :selector-member-function (find/function?  state member)
    :selector-member-not (seq (by-state state (:selectors member)))
    true))


(defn by-state
  [state nodes]
  (keep
   (fn [node]
     (case (:type node)

       :selector
       (when (every? #(used-member? state %) (:members node))
         node)

       :style-rule
       (let [selectors (by-state state (:selectors node))]
         (when (seq selectors)
           (assoc node :selectors selectors)))

       :media-rule
       (let [rules (by-state state (:rules node))]
         (when (seq rules)
           (assoc node :rules rules)))

       node))
   nodes))


(defn used-declaration?
  [context declaration]
  (let [declaration-meta (:meta declaration)]
    (cond
      (= :variable (:type declaration-meta))
      (find/used-variable? context declaration)

      (:variables declaration-meta)
      (every? (fn [variable]
                (let [[value default] (re-seq #"[^,]+" variable)]
                  (or (find/variable? context value) default)))
              (:variables declaration-meta))

      :else true)))


(defn by-context
  [context nodes]
  (keep
   (fn [node]
     (case (:type node)

       :declaration
       (when (used-declaration? context node)
         node)

       :style-rule
       (let [declarations (by-context context (:declarations node))]
         (when (seq declarations)
           (assoc node :declarations declarations)))

       :media-rule
       (let [rules (by-context context (:rules node))]
         (when (seq rules)
           (assoc node :rules rules)))

       :keyframes-rule
       (when (find/keyframes? context node)
         node)

       node))
   nodes))
