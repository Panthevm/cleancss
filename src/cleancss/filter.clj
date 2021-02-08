(ns cleancss.filter
  (:require
   [clojure.string :as string]))


(defn used-attribute?
  [app member]
  (some (fn [[attribute-name attribute-value]]
          (when (= attribute-name (:name member))
            (condp = (-> member :operator :name)
              "="  (= attribute-value (:attribute member))
              "^=" (string/starts-with? attribute-value (:attribute member))
              "$=" (string/ends-with?   attribute-value (:attribute member))
              "*=" (string/includes?    attribute-value (:attribute member))
              "~=" (some #(= (:attribute member) %)
                         (string/split attribute-value #" "))
              "|=" (or (= attribute-value (:attribute member))
                       (string/starts-with? attribute-value (str (:attribute member) "-")))
              true)))
        (:attributes app)))


(defn used-selector?
  [app member]
  (condp = (:group member)
    :class      (contains? (:classes     app) (:name member))
    :type       (contains? (:types       app) (:name member))
    :pseudo     (contains? (:pseudos     app) (:name member))
    :identifier (contains? (:identifiers app) (:name member))
    true))


(declare remove-unused-selectors)


(defn used-member?
  [app member]
  (condp = (:type member)
    :selector-simple-member   (used-selector?  app member)
    :selector-attribute       (used-attribute? app member)
    :selector-member-not      (seq (remove-unused-selectors app (:selectors member)))
    :selector-member-function (contains? (:functions app) (:function member))
    true))


(defn remove-unused-selectors
  [app selectors]
  (filter (fn [selector]
            (every? (fn [member]
                      (used-member? app member))
                    (:members selector)))
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
                  :variables      #{}
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
              (recur next-nodes (update context :variables conj (:property node)))

              (-> node :meta :variables)
              (recur next-nodes (update context :used-variables into (-> node :meta :variables)))

              :else (recur next-nodes context)))

          (= :style-rule node-type) (recur (into next-nodes (:declarations node)) context)
          (= :media-rule node-type) (recur (into next-nodes (:rules        node)) context)
          :else                     (recur next-nodes                             context)))

      context)))


(defn clear-declarations
  [context declarations]
  (let [unsuded? (complement contains?)]
    (remove
     (fn [declaration]

       (or (and (= :variable (-> declaration :meta :type))
                (unsuded? (:used-variables context) (:property declaration)))

           (and (-> declaration :meta :variables)
                (some (partial unsuded? (:variables context))
                      (-> declaration :meta :variables)))))

     declarations)))


(defn remove-duplicate-declarations
  [declarations]
  (vals
   (reduce
    (fn [accumulator declaration]
      (assoc accumulator (:property declaration) declaration))
    {} declarations)))

;;
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
