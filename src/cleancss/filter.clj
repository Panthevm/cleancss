(ns cleancss.filter
  (:require
   [clojure.string :as string]))


(defn used-namespace?
  [namespaces member]
  (let [namespace-name (->> member :value drop-last (apply str))]
    (or (string/blank? namespace-name)
        (contains? namespaces namespace-name))))


(defn used-attribute?
  [attributes member]
  (let [operator (-> member :operator :name)]
    (some (fn [[attribute-name attribute-value]]
            (when (= attribute-name (:name member))
              (cond
                (= "=" operator)
                (= attribute-value (:attribute member))

                (= "~" operator)
                (let [values (string/split attribute-value #" ")]
                  (some #{(:attribute member)} values ))

                (= "|" operator)
                (or (= attribute-value (:attribute member))
                    (string/starts-with? attribute-value
                                         (str (:attribute member) "-")))

                (= "^" operator)
                (string/starts-with? attribute-value (:attribute member))

                (= "$" operator)
                (string/ends-with? attribute-value (:attribute member))

                (= "*" operator)
                (string/includes? attribute-value (:attribute member))

                :else true)))
          attributes)))


(declare used-selector?)


(defn used-member?
  [application member]
  (cond
    (= :selector-simple-member (:type member))
    (cond

      (string/starts-with? (:value member) ".")
      (contains? (:classes application) (subs (:value member) 1))

      (string/starts-with? (:value member) ":")
      (some (partial string/starts-with? (:value member))
            (:pseudos application))

      (string/starts-with? (:value member) "#")
      (contains? (:identifiers application) (subs (:value member) 1))

      (string/ends-with? (:value member) "|")
      (or (= "|" (:value member))
          (used-namespace? (:namespaces application) member))

      :else
      (contains? (:types application) (:value member)))

    (= :selector-attribute (:type member))
    (used-attribute? (:attributes application) member)

    (= :selector-member-function (:type member))
    (some (partial string/starts-with? (:name member))
          (:functions application))

    (= :selector-combinator (:type member))
    true

    (= :selector-member-not (:type member))
    (not (every? (partial used-selector? application)
                 (:selectors member)))))


(defn used-selector?
  [selectors selector]
  (every? (partial used-member? selectors)
          (:members selector)))


(defn remove-unused-selectors
  [application selectors]
  (filter (partial used-selector? application)
          selectors))


(defn remove-unused-stylesheets
  [application stylesheets]
  (loop [processing stylesheets
         processed  []]
    (if processing
      (let [stylesheet       (first processing)
            type-stylesheet  (:type stylesheet)
            next-stylesheets (next  processing)]
        (cond

          (= :style-rule type-stylesheet)
          (let [used-selectors (remove-unused-selectors application (:selectors stylesheet))]
            (if (seq used-selectors)
              (recur next-stylesheets (conj processed (assoc stylesheet :selectors used-selectors)))
              (recur next-stylesheets processed)))

          (= :media-rule type-stylesheet)
          (let [used-stylesheets (remove-unused-stylesheets application (:rules stylesheet))]
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

(defn remove-by-context
  [context stylesheets]
  (loop [processing stylesheets
         processed  []]
    (if processing
      (let [stylesheet      (first processing)
            type-stylesheet (:type stylesheet)]
        (cond

          (= :style-rule type-stylesheet)
          (let [new-declarations (clear-declarations context (:declarations stylesheet))]
            (if (seq new-declarations)
              (recur (next processing) (conj processed (assoc stylesheet :declarations new-declarations)))
              (recur (next processing) processed)))

          (= :media-rule type-stylesheet)
          (let [media-stylesheets (remove-by-context context (:rules stylesheet))]
            (if (seq media-stylesheets)
              (recur (next processing) (conj processed (assoc stylesheet :rules media-stylesheets)))
              (recur (next processing) processed)))

          (= :keyframes-rule type-stylesheet)
          (if (contains? (:animations context) (:name stylesheet))
            (recur (next processing) (conj processed stylesheet))
            (recur (next processing) processed))

          :else
          (recur (next processing) processed)))
      processed)))


(defn make-clean
  [application stylesheets]
  (let [used-stylesheets (remove-unused-stylesheets application stylesheets)
        context          (get-context used-stylesheets)]
    (remove-by-context context used-stylesheets)))
