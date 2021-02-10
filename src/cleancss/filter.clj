(ns cleancss.filter
  (:require
   [cleancss.find        :as find]
   [cleancss.compression :as compress]
   [clojure.string :as string]))


(declare remove-unused-selectors)


(defn used-member?
  [app member]
  (condp = (:type member)
    :selector-simple-member
    (condp = (:group member)
      :class      (find/class?      app member)
      :type       (find/type?       app member)
      :pseudo     (find/pseudo?     app member)
      :identifier (find/identifier? app member)
      true)
    :selector-attribute       (find/attribute? app member)
    :selector-member-function (find/function? app member)
    :selector-member-not      (seq (remove-unused-selectors app (:selectors member)))
    true))


(defn update-used-members
  [app members]
  (map (fn [member]
         (when (used-member? app member)
           (if (= :class (:group member))
             (assoc member :value
                    (str "." (get-in app [:classes (:name member)])))
             member)))
       members))


(defn remove-unused-selectors
  [app selectors]
  (keep (fn [selector]
          (let [new-members (update-used-members app (:members selector))]
            (when (every? identity new-members)
              (assoc selector :members new-members))))
        selectors))


(defn remove-unused-stylesheets
  [app stylesheets]
  (loop [nodes       (seq stylesheets)
         accumulator (transient [])]
    (if nodes
      (let [node       (first nodes)
            next-nodes (next  nodes)
            node-type  (:type node)]
        (cond

          (= :style-rule node-type)
          (let [selectors (remove-unused-selectors app (:selectors node))]
            (if (seq selectors)
              (recur next-nodes (conj! accumulator (assoc node :selectors selectors)))
              (recur next-nodes accumulator)))

          (= :media-rule node-type)
          (let [rules (remove-unused-stylesheets app (:rules node))]
            (if (seq rules)
              (recur next-nodes (conj! accumulator (assoc node :rules rules)))
              (recur next-nodes accumulator)))

          :else
          (recur next-nodes (conj! accumulator node))))

      (persistent! accumulator))))


(defn get-context
  [stylesheets]
  (loop [nodes   stylesheets
         context {:animations     #{}
                  :variables      {}
                  :used-variables #{}}]
    (if nodes
      (let [node       (first nodes)
            next-nodes (next  nodes)
            node-type  (:type node)]
        (cond

          (= :declaration node-type)
          (let [meta-type (-> node :meta :type)]
            (cond

              (= :animation meta-type)
              (recur next-nodes (update context :animations conj (-> node :meta :animation)))

              (= :variable meta-type)
              (recur next-nodes (update context :variables
                                        (fn [variables]
                                          (assoc variables (:property node)
                                                 (str "--" (compress/short-name (count variables)))))))

              (-> node :meta :variables)
              (recur next-nodes (update context :used-variables
                                        into (->> node :meta :variables
                                                  (map (fn [variable]
                                                         (first (string/split variable #",")))))))

              :else (recur next-nodes context)))

          (= :style-rule node-type) (recur (into next-nodes (:declarations node)) context)
          (= :media-rule node-type) (recur (into next-nodes (:rules        node)) context)
          :else                     (recur next-nodes                             context)))

      context)))


(defn expression-variables-resolve
  [context declaration]
  (reduce
   (fn [expression variable]
     (when expression
       (let [[value default] (string/split variable #",")]
         (cond
           (find/variable? context value)
           (string/replace expression (re-pattern variable)
                           (get-in context [:variables value]))

           default
           (string/replace expression (re-pattern (str "var\\(" variable "\\)")) default)))))
   (-> declaration :expression)
   (-> declaration :meta :variables)))


(defn clear-declarations
  [context declarations]
  (keep (fn [declaration]
          (let [declaration-meta (:meta declaration)]
            (cond
              (= :variable (:type declaration-meta))
              (when (find/used-variable? context declaration)
                (assoc declaration :property (get-in context [:variables (:property declaration)])))

              (:variables declaration-meta)
              (let [new-expression (expression-variables-resolve context declaration)]
                (when new-expression
                  (assoc declaration :expression new-expression)))

              :else declaration)))
        declarations))


(defn remove-duplicate-declarations
  [declarations]
  (vals
   (reduce
    (fn [accumulator declaration]
      (assoc accumulator (:property declaration) declaration))
    {} declarations)))


(defn remove-by-context
  [context stylesheets]
  (loop [nodes        (seq stylesheets)
         schema       {}
         accumulator  []]
    (if nodes
      (let [node       (first nodes)
            next-nodes (next nodes)
            node-type  (:type node)]
        (cond

          (= :style-rule node-type)
          (let [new-declarations (clear-declarations context (:declarations node))]
            (if (seq new-declarations)
              (let [new-node   (assoc node :declarations new-declarations)
                    identifier (hash (:selectors node))
                    new-schema (if (contains? schema identifier)
                                 (update-in schema [identifier :declarations] into new-declarations)
                                 (assoc schema identifier new-node))]
                (recur next-nodes new-schema []))
              (recur next-nodes schema [])))

          (= :media-rule node-type)
          (let [media-nodes (remove-by-context context (:rules node))]
            (if (seq media-nodes)
              (recur next-nodes schema (conj accumulator (assoc node :rules media-nodes)))
              (recur next-nodes schema accumulator)))

          (= :keyframes-rule node-type)
          (if (contains? (:animations context) (:name node))
            (recur next-nodes schema (conj accumulator node))
            (recur next-nodes schema accumulator))

          :else
          (recur next-nodes schema (conj accumulator node))))

      (into accumulator
            (map (fn [[_ style-rule]]
                   (update style-rule :declarations remove-duplicate-declarations))
                 schema)))))


(defn make-clean
  [application stylesheets]
  (let [used-stylesheets (remove-unused-stylesheets application stylesheets)
        context          (get-context used-stylesheets)]
    (remove-by-context context used-stylesheets)))
