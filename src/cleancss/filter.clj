(ns cleancss.filter
  (:require
   [cleancss.find :as find]))


(declare clean-by-state)


(defn used-member?
  [state member]
  (condp = (:type member)
    :selector-simple-member
    (condp = (:group member)
      :class      (find/class?      state member)
      :type       (find/type?       state member)
      :pseudo     (find/pseudo?     state member)
      :identifier (find/identifier? state member)
      true)
    :selector-attribute       (find/attribute? state member)
    :selector-member-function (find/function?  state member)
    :selector-member-not      (seq (clean-by-state state (:selectors member)))
    true))


(defn clean-by-state
  [state stylesheets]
  (loop [nodes       (seq stylesheets)
         accumulator (transient [])]
    (if nodes
      (let [node       (first nodes)
            next-nodes (next  nodes)
            node-type  (:type node)]
        (cond

          (= :selector node-type)
          (if (every? #(used-member? state %) (:members node))
            (recur next-nodes (conj! accumulator node))
            (recur next-nodes accumulator))

          (= :style-rule node-type)
          (let [selectors (clean-by-state state (:selectors node))]
            (if (seq selectors)
              (recur next-nodes (conj! accumulator (assoc node :selectors selectors)))
              (recur next-nodes accumulator)))

          (= :media-rule node-type)
          (let [rules (clean-by-state state (:rules node))]
            (if (seq rules)
              (recur next-nodes (conj! accumulator (assoc node :rules rules)))
              (recur next-nodes accumulator)))

          :else
          (recur next-nodes (conj! accumulator node))))

      (persistent! accumulator))))


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


(defn clean-by-context
  [context stylesheets]
  (loop [nodes       (seq stylesheets)
         accumulator (transient [])]
    (if nodes
      (let [node       (first nodes)
            next-nodes (next  nodes)
            node-type  (:type node)]
        (cond

          (= :declaration node-type)
          (if (used-declaration? context node)
            (recur next-nodes (conj! accumulator node))
            (recur next-nodes accumulator))

          (= :style-rule node-type)
          (let [declarations (clean-by-context context (:declarations node))]
            (if (seq declarations)
              (recur next-nodes (conj! accumulator (assoc node :declarations declarations)))
              (recur next-nodes accumulator)))

          (= :media-rule node-type)
          (let [rules (clean-by-context context (:rules node))]
            (if (seq rules)
              (recur next-nodes (conj! accumulator (assoc node :rules rules)))
              (recur next-nodes accumulator)))

          (= :keyframes-rule node-type)
          (if (find/keyframes? context node)
            (recur next-nodes (conj! accumulator node))
            (recur next-nodes accumulator))

          :else 
          (recur next-nodes (conj! accumulator node))))

      (persistent! accumulator))))
