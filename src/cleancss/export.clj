(ns cleancss.export
  (:require
   [clojure.java.io :as io])
  (:import
   [com.helger.css.writer
    CSSWriter
    CSSWriterSettings]

   [com.helger.css
    ECSSVersion]

   [com.helger.css.decl
    CSSStyleRule
    CSSDeclaration
    CSSExpression

    CSSMediaRule
    CSSMediaQuery
    CSSMediaQuery$EModifier
    CSSMediaExpression

    CSSKeyframesRule
    CSSKeyframesBlock

    CascadingStyleSheet

    CSSSelector
    ICSSSelectorMember
    CSSSelectorMemberNot
    CSSSelectorAttribute
    ECSSAttributeOperator
    ECSSSelectorCombinator
    CSSSelectorSimpleMember
    CSSSelectorMemberFunctionLike]))


(defmulti datafy :type)

(defmethod datafy :declaration
  [schema]
  (CSSDeclaration.
   (-> schema :property)
   (-> schema :expression CSSExpression/createSimple)
   (-> schema :important?)))


(defmethod datafy :selector-simple-member
  [schema]
  (CSSSelectorSimpleMember.
   (-> schema :value)))


(defmethod datafy :selector-combinator
  [schema]
  (case (:name schema)
    "+" ECSSSelectorCombinator/PLUS
    ">" ECSSSelectorCombinator/GREATER
    "~" ECSSSelectorCombinator/TILDE
    " " ECSSSelectorCombinator/BLANK))


(defmethod datafy :selector-member-not
  [schema]
  (CSSSelectorMemberNot.
   (->> schema :selectors (map datafy))))


(defmethod datafy :attribute-operator
  [schema]
  (case (:name schema)
    "="  ECSSAttributeOperator/EQUALS
    "~=" ECSSAttributeOperator/INCLUDES
    "|=" ECSSAttributeOperator/DASHMATCH
    "^=" ECSSAttributeOperator/BEGINMATCH
    "$=" ECSSAttributeOperator/ENDMATCH
    "*=" ECSSAttributeOperator/CONTAINSMATCH))


(defmethod datafy :selector-member-function
  [schema]
  (CSSSelectorMemberFunctionLike.
   (-> schema :name)
   (-> schema :expression CSSExpression/createSimple)))


(defmethod datafy :selector-attribute
  [schema]
  (if (-> schema :operator :name)
    (CSSSelectorAttribute.
     (-> schema :namespace)
     (-> schema :name)
     (-> schema :operator datafy)
     (-> schema :attribute))
    (CSSSelectorAttribute.
     (-> schema :namespace)
     (-> schema :name))))


(defmethod datafy :selector
  [schema]
  (let [object (CSSSelector.)]
    (doseq [member (:members schema)]
      (.addMember object (datafy member)))
    object))


(defmethod datafy :style-rule
  [schema]
  (let [object (CSSStyleRule.)]
    (doseq [selector (:selectors schema)]
      (.addSelector object (datafy selector)))
    (doseq [declaration (:declarations schema)]
      (.addDeclaration object (datafy declaration)))
    object))


(defmethod datafy :keyframes-block
  [schema]
  (let [object (CSSKeyframesBlock. (:selectors schema))]
    (doseq [declaration (:declarations schema)]
      (.addDeclaration object (datafy declaration)))
    object))


(defmethod datafy :keyframes-rule
  [schema]
  (let [object (CSSKeyframesRule. (:declaration schema) (:name schema))]
    (doseq [block (:blocks schema)]
      (.addBlock object (datafy block)))
    object))


(defmethod datafy :media-expression
  [schema]
  (CSSMediaExpression.
   (-> schema :feature)
   (-> schema :value CSSExpression/createSimple)))


(defmethod datafy :media-query
  [schema]
  (letfn [(get-modifier [schema]
            (cond
              (:only? schema) CSSMediaQuery$EModifier/ONLY
              (:not schema)   CSSMediaQuery$EModifier/NOT
              :else           CSSMediaQuery$EModifier/NONE))]
    (let [object
          (CSSMediaQuery.
           (-> schema get-modifier)
           (-> schema :medium))]
      (doseq [expression (:expressions schema)]
        (.addMediaExpression object (datafy expression)))
      object)))


(defmethod datafy :media-rule
  [schema]
  (let [object (CSSMediaRule.)]
    (doseq [query (:queries schema)]
      (.addMediaQuery object (datafy query)))
    (doseq [rule (:rules schema)]
      (.addRule object (datafy rule)))
    object))


(defn to-string
  [schema]
  (let [cascading-object (CascadingStyleSheet.)
        settings-object  (CSSWriterSettings. ECSSVersion/CSS30 true)
        writer-object    (doto (CSSWriter. settings-object)
                           (.setWriteHeaderText false))]
    (doseq [node (remove nil? schema)]
      (.addRule cascading-object (datafy node)))
    (.getCSSAsString writer-object cascading-object)))


(defn to-file
  [configuration stylesheets]
  (let [cascading-object (CascadingStyleSheet.)
        settings-object  (CSSWriterSettings. ECSSVersion/CSS30 true)
        writer-object    (doto (CSSWriter. settings-object)
                           (.setWriteHeaderText false)
                           #_(.setContentCharset "UTF-8"))]
    (doseq [style (remove nil? stylesheets)]
      (.addRule cascading-object (datafy style)))
    (-> configuration :output-file io/file io/make-parents)
    (.writeCSS writer-object
               cascading-object
               (io/writer (:output-file configuration)))))
