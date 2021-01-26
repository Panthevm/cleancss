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
      (let [stylesheet (first processing)]
        (cond

          (= :style-rule (:type stylesheet))
          (let [updated-stylesheet
                (update stylesheet :selectors
                        (partial remove-unused-selectors application))]
            (if (seq (:selectors updated-stylesheet))
              (recur (next processing) (cons updated-stylesheet processed))
              (recur (next processing) processed)))

          (= :media-rule (:type stylesheet))
          (let [updated-stylesheet
                (update stylesheet :rules
                        (partial remove-unused-stylesheets application))]
            (if (seq (:rules updated-stylesheet))
              (recur (next processing) (cons updated-stylesheet processed))
              (recur (next processing) processed)))

          (= :keyframes-rule (:type stylesheet))
          (recur (next processing) (concat processed (list stylesheet)))))

      processed)))


(defn remove-unused-keyframes
  [stylesheets]
  (loop [processing stylesheets
         processed  []
         animations #{}]
    (if processing
      (let [stylesheet (first processing)]
        (cond

          (= :style-rule (:type stylesheet))
          (recur (next processing)
                 (cons stylesheet processed)
                 (conj animations
                       (some->> (:declarations stylesheet)
                                (filter (comp #{"animation"} :property))
                                first
                                :expression
                                (re-seq #"[^ ]+")
                                first)))

          (= :media-rule (:type stylesheet))
          (let [[media-animations media-stylesheets] (remove-unused-keyframes (:rules stylesheet))]
            (recur (next processing) (cons stylesheet processed) (into animations media-animations)))

          (= :keyframes-rule (:type stylesheet))
          (if (contains? animations (:name stylesheet))
            (recur (next processing) (cons stylesheet processed) animations)
            (recur (next processing) processed                   animations))))
      [animations processed])))


(defn make-clean
  [application stylesheets]
  (cond-> (remove-unused-stylesheets application stylesheets)
    (:keyframes? application)
    (-> remove-unused-keyframes second)))
