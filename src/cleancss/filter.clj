(ns cleancss.filter
  (:require
   [clojure.string :as string]))


(defn used-attribute?
  [attributes member]
  (let [operator (-> member :operator :name)]
    (some (fn [[attribute-name attribute-value]]
            (when (= attribute-name (:name member))
              (let [attribute (:attribute member)]
                (cond
                  (= "=" operator)
                  (= attribute-value attribute)

                  (= "~" operator)
                  (let [values (string/split attribute-value #" ")]
                    (some #{attribute} values))

                  (= "|" operator)
                  (or (= attribute-value attribute)
                      (string/starts-with? attribute-value (str attribute "-")))

                  (= "^" operator)
                  (string/starts-with? attribute-value attribute)

                  (= "$" operator)
                  (string/ends-with? attribute-value attribute)

                  (= "*" operator)
                  (string/includes? attribute-value attribute)

                  :else true))))
          attributes)))


(declare remove-unused-selectors)


(defn used-member?
  [app member]
  (let [member-type (:type member)]
    (cond
      (= :selector-simple-member member-type)
      (let [member-value (:value member)]
        (cond

          (string/starts-with? member-value ".")
          (contains? (:classes app) (subs member-value 1))

          (string/starts-with? member-value "#")
          (contains? (:identifiers app) (subs member-value 1))

          (string/starts-with? member-value ":")
          (some (partial string/starts-with? member-value) (:pseudos app))

          (string/ends-with? member-value "|")
          true

          :else
          (contains? (:types app) (:value member))))

      (= :selector-attribute member-type)
      (used-attribute? (:attributes app) member)

      (= :selector-member-function member-type)
      (some (partial string/starts-with? (:name member)) (:functions app))

      (= :selector-combinator member-type)
      true

      (= :selector-member-not member-type)
      (empty? (remove-unused-selectors app (:selectors member))))))


(defn remove-unused-selectors
  [app selectors]
  (filter (fn [selector]
            (every? (fn [member]
                      (used-member? app member))
                    (:members selector)))
          selectors))


(defn remove-unused-stylesheets
  [app stylesheets]
  (loop [processing stylesheets
         processed  []]
    (if processing
      (let [stylesheet       (first processing)
            type-stylesheet  (:type stylesheet)
            next-stylesheets (next  processing)]
        (cond

          (= :style-rule type-stylesheet)
          (let [used-selectors (remove-unused-selectors app (:selectors stylesheet))]
            (if (seq used-selectors)
              (recur next-stylesheets (conj processed (assoc stylesheet :selectors used-selectors)))
              (recur next-stylesheets processed)))

          (= :media-rule type-stylesheet)
          (let [used-stylesheets (remove-unused-stylesheets app (:rules stylesheet))]
            (if (seq used-stylesheets)
              (recur next-stylesheets (conj processed (assoc stylesheet :rules used-stylesheets)))
              (recur next-stylesheets processed)))

          :else
          (recur next-stylesheets (conj processed stylesheet))))
      processed)))


(defn get-context
  [stylesheets]
  (loop [nodes   stylesheets
         context {:animations     #{}
                  :variables      #{}
                  :used-variables #{}}]
    (if nodes
      (let [node      (first nodes)
            node-type (:type node)]
        (cond

          (= :declaration node-type)
          (cond

            (= "animation" (:property node))
            (let [animation-name (re-find #"\w+" (:expression node))]
              (recur (next nodes) (update context :animations conj animation-name)))

            (string/starts-with? (:property node) "--")
            (recur (next nodes) (update context :variables conj (:property node)))

            (string/includes? (:expression node) "var(")
            (let [used-variables (re-seq #"(?<=var\()(?:.*?)(?=\))" (:expression node))]
              (recur (next nodes) (update context :used-variables into used-variables)))

            :else (recur (next nodes) context))

          (= :style-rule node-type) (recur (into (next nodes) (:declarations node)) context)
          (= :media-rule node-type) (recur (into (next nodes) (:rules        node)) context)
          :else                     (recur (next nodes)                             context)))

      context)))


(defn clear-declarations
  [context declarations]
  (let [unsuded? (complement contains?)]
    (remove
     (fn [declaration]
       (or (and (string/starts-with? (:property declaration) "--")
                (unsuded? (:used-variables context) (:property declaration)))
           (and (string/includes? (:expression declaration) "var(")
                (some (partial unsuded? (:variables context))
                      (re-seq #"(?<=var\()(?:.*?)(?=\))" (:expression declaration))))))
     declarations)))


(defn remove-duplicate-declarations
  [declarations]
  (vals
   (reduce
    (fn [accumulator declaration]
      (assoc accumulator (:property declaration) declaration))
    {} declarations)))


(defn remove-by-context
  [context stylesheets]
  (loop [processing stylesheets
         schema     {}
         processed  []]
    (if processing
      (let [stylesheet      (first processing)
            type-stylesheet (:type stylesheet)]
        (cond

          (= :style-rule type-stylesheet)
          (let [new-declarations (clear-declarations context (:declarations stylesheet))]
            (if (seq new-declarations)
              (let [new-stylesheet (assoc stylesheet :declarations new-declarations)
                    members        (->> stylesheet :selectors (mapcat (comp (partial map :value) :members)) vec)
                    new-schema     (if (contains? schema members)
                                     (update-in schema [members :declarations] into new-declarations)
                                     (assoc  schema members new-stylesheet))]
                (recur (next processing) new-schema []))
              (recur (next processing) schema [])))

          (= :media-rule type-stylesheet)
          (let [media-stylesheets (remove-by-context context (:rules stylesheet))]
            (if (seq media-stylesheets)
              (recur (next processing) schema (conj processed (assoc stylesheet :rules media-stylesheets)))
              (recur (next processing) schema processed)))

          (= :keyframes-rule type-stylesheet)
          (if (contains? (:animations context) (:name stylesheet))
            (recur (next processing) schema (conj processed stylesheet))
            (recur (next processing) schema processed))

          :else
          (recur (next processing) schema (conj processed stylesheet))))

      (into processed
            (map (fn [[_ style-rule]]
                   (update style-rule :declarations remove-duplicate-declarations))
                 schema)))))


(defn make-clean
  [application stylesheets]
  (let [used-stylesheets (remove-unused-stylesheets application stylesheets)
        context          (get-context used-stylesheets)]
    (remove-by-context context used-stylesheets)))
