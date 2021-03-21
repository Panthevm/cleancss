(ns cleancss.data
  (:require
   [clojure.core.protocols :as cp]
   [clojure.java.io        :as io]
   [clojure.string         :as string])
  (:import

   [com.helger.css.decl
    CascadingStyleSheet

    CSSDeclaration
    CSSExpression


    CSSSelector
    CSSSelectorMemberNot
    CSSSelectorAttribute
    ECSSAttributeOperator
    ECSSSelectorCombinator
    CSSSelectorSimpleMember
    CSSSelectorMemberFunctionLike

    CSSStyleRule

    CSSMediaRule
    CSSMediaQuery
    CSSMediaQuery$EModifier
    CSSMediaExpression

    CSSKeyframesRule
    CSSKeyframesBlock]

   [com.helger.css
    ECSSVersion]

   [java.nio.charset
    StandardCharsets]

   [com.helger.css.reader
    CSSReader]

   [com.helger.css.writer
    CSSWriter
    CSSWriterSettings]))

(defmulti make-object :type)
(defmacro deftransform
  "Java -> Clojure -> Java"
  [object-type identifier to-schema to-object]
  `(do

     (extend-type ~object-type
       cp/Datafiable
       (datafy [stylesheet-object#]
         (merge
          (~to-schema stylesheet-object#)
          {:type ~identifier})))

     (defmethod make-object ~identifier
       [stylesheet-schema#]
       (~to-object stylesheet-schema#))))


(deftransform ECSSSelectorCombinator :selector-combinator
  (fn [^ECSSSelectorCombinator object]
    {:name (-> object .getName)})
  (fn [schema]
    (ECSSSelectorCombinator/getFromNameOrNull (:name schema))))


(deftransform ECSSAttributeOperator :attribute-operator
  (fn [^ECSSAttributeOperator object]
    {:name (-> object .getName)})
  (fn [schema]
    (ECSSAttributeOperator/getFromNameOrNull (:name schema))))


(deftransform CSSSelectorMemberNot :selector-member-not
  (fn [^CSSSelectorMemberNot object]
    {:selectors (->> object .getAllSelectors (map cp/datafy))})
  (fn [schema]
    (CSSSelectorMemberNot.
     (->> schema :selectors (map make-object)))))


(deftransform CSSSelectorMemberFunctionLike :member-function
  (fn [^CSSSelectorMemberFunctionLike object]
    {:name       (.getFunctionName object)
     :expression (-> object .getParameterExpression .getAsCSSString)})
  (fn [schema]
    (CSSSelectorMemberFunctionLike.
     (-> schema :name)
     (-> schema :expression CSSExpression/createSimple))))


(deftransform CSSSelectorAttribute :selector-attribute
  (fn [^CSSSelectorAttribute object]
    {:name      (-> object .getAttrName)
     :operator  (-> object .getOperator cp/datafy)
     :namespace (-> object .getNamespacePrefix)
     :attribute (-> object .getAttrValue)})
  (fn [schema]
    (if (-> schema :operator :name)
      (CSSSelectorAttribute.
       (-> schema :namespace)
       (-> schema :name)
       (-> schema :operator make-object)
       (-> schema :attribute))
      (CSSSelectorAttribute.
       (-> schema :namespace)
       (-> schema :name)))))


(deftransform CSSDeclaration :declaration
  (fn [^CSSDeclaration object]
    {:property   (-> object .getProperty)
     :expression (-> object .getExpressionAsCSSString)
     :important? (-> object .isImportant)})
  (fn [schema]
    (CSSDeclaration.
     (-> schema :property)
     (-> schema :expression CSSExpression/createSimple)
     (-> schema :important?))))


(deftransform CSSMediaExpression :media-expression
  (fn [^CSSMediaExpression object]
    {:value   (-> object .getValue .getAsCSSString)
     :feature (-> object .getFeature)})
  (fn [schema]
    (CSSMediaExpression.
     (-> schema :feature)
     (-> schema :value CSSExpression/createSimple))))


(deftransform CSSSelectorSimpleMember :member
  (fn [^CSSSelectorSimpleMember object]
    {:value (.getValue object)
     :group (cond
              (.isClass  object) :class
              (.isHash   object) :identifier
              (.isPseudo object) :pseudo
              :else              :type)})
  (fn [schema]
    (CSSSelectorSimpleMember.
     (:value schema))))


(deftransform CSSSelector :selector
  (fn [^CSSSelector object]
    {:members (->> object .getAllMembers (map cp/datafy))})
  (fn [schema]
    (let [object (CSSSelector.)]
      (doseq [member (:members schema)]
        (.addMember object (make-object member)))
      object)))


(deftransform CSSStyleRule :style-rule
  (fn [^CSSStyleRule object]
    {:selectors    (->> object .getAllSelectors    (map cp/datafy))
     :declarations (->> object .getAllDeclarations (map cp/datafy))})
  (fn [schema]
    (let [object (CSSStyleRule.)]
      (doseq [selector (:selectors schema)]
        (.addSelector object (make-object selector)))
      (doseq [declaration (:declarations schema)]
        (.addDeclaration object (make-object declaration)))
      object)))


(deftransform CSSMediaRule :media-rule
  (fn [^CSSMediaRule object]
    {:rules   (->> object .getAllRules        (map cp/datafy))
     :queries (->> object .getAllMediaQueries (map cp/datafy))})
  (fn [schema]
    (let [object (CSSMediaRule.)]
      (doseq [query (:queries schema)]
        (.addMediaQuery object (make-object query)))
      (doseq [rule (:rules schema)]
        (.addRule object (make-object rule)))
      object)))


(deftransform CSSMediaQuery :media-query
  (fn [^CSSMediaQuery object]
    {:not?        (->  object .isNot)
     :only?       (->  object .isOnly)
     :medium      (->  object .getMedium)
     :expressions (->> object .getAllMediaExpressions (map cp/datafy))})
  (fn [schema]
    (let [object
          (CSSMediaQuery.
           (cond
             (:only? schema) CSSMediaQuery$EModifier/ONLY
             (:not?  schema) CSSMediaQuery$EModifier/NOT
             :else           CSSMediaQuery$EModifier/NONE)
           (:medium schema))]
      (doseq [expression (:expressions schema)]
        (.addMediaExpression object (make-object expression)))
      object)))


(deftransform CSSKeyframesRule :keyframes-rule
  (fn [^CSSKeyframesRule object]
    {:declaration (->  object .getDeclaration)
     :name        (->  object .getAnimationName)
     :blocks      (->> object .getAllBlocks (map cp/datafy))})
  (fn [schema]
    (let [object
          (CSSKeyframesRule. (:declaration schema) (:name schema))]
      (doseq [block (:blocks schema)]
        (.addBlock object (make-object block)))
      object)))


(deftransform CSSKeyframesBlock :keyframes-block
  (fn [^CSSKeyframesBlock object]
    {:selectors    (->  object .getAllKeyframesSelectors)
     :declarations (->> object .getAllDeclarations (map cp/datafy))})
  (fn [schema]
    (let [object (CSSKeyframesBlock. (:selectors schema))]
      (doseq [declaration (:declarations schema)]
        (.addDeclaration object (make-object declaration)))
      object)))


(defn resource->schema
  [resource]
  (->> 
   (CSSReader/readFromFile
    (io/file resource)
    StandardCharsets/UTF_8
    ECSSVersion/CSS30)
   .getAllRules
   (mapv cp/datafy)))


(defn string->schema
  [^String value]
  (let [object (CSSReader/readFromString value ECSSVersion/CSS30)]
    (mapv cp/datafy (.getAllRules object))))


(defn schema->string
  [schema]
  (let [cascading* (CascadingStyleSheet.)
        settings*  (CSSWriterSettings. ECSSVersion/CSS30 true)
        writer*    (doto (CSSWriter. settings*)
                     (.setWriteHeaderText false))]
    (doseq [node schema]
      (.addRule cascading* (make-object node)))
    (.getCSSAsString writer* cascading*)))


(defn schema->resource
  [schema resource]
  (let [cascading* (CascadingStyleSheet.)
        settings*  (CSSWriterSettings. ECSSVersion/CSS30 true)
        writer*    (doto (CSSWriter. settings*)
                     (.setWriteHeaderText false))]
    (doseq [node schema]
      (.addRule cascading* (make-object node)))
    (->> resource io/file io/make-parents)
    (.writeCSS writer* cascading* (io/writer resource))))
