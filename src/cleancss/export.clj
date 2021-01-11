(ns cleancss.export
  (:require
   [clojure.java.io :as io])
  (:import
   [com.helger.css.writer
    CSSWriter
    CSSWriterSettings]

   [com.helger.css
    ECSSVersion]

   [java.nio.charset
    StandardCharsets]

   [com.helger.css.decl
    CSSMediaRule
    CSSMediaQuery
    CSSMediaExpression
    CSSStyleRule
    CSSKeyframesRule
    CSSKeyframesBlock
    CSSSelector
    CSSSelectorSimpleMember
    CSSExpression
    CSSDeclaration
    CascadingStyleSheet]

   [com.helger.css.reader
    CSSReader]

   [com.helger.css.writer
    CSSWriter
    CSSWriterSettings]))


(defmulti datafy :type)


(defmethod datafy :declaration
  [schema]
  (CSSDeclaration.
   (-> schema :property)
   (-> schema :expression CSSExpression/createSimple)
   (-> schema :important?)))


(defmethod datafy :media-expression
  [schema]
  (CSSMediaExpression.
   (-> schema :feature)
   (-> schema :value CSSExpression/createSimple)))


(defmethod datafy :style-rule
  [schema]
  (let [object (CSSStyleRule.)]
    (doseq [selector (:selectors schema)]
      (.addSelector object (CSSSelectorSimpleMember. selector)))
    (doseq [declaration (:declarations schema)]
      (.addDeclaration object (datafy declaration)))
    object))


(defmethod datafy :media-rule
  [schema]
  (let [object (CSSMediaRule.)]
    (doseq [query (:queries schema)]
      (.addMediaQuery object (datafy query)))
    (doseq [rule (:rules schema)]
      (.addRule object (datafy rule)))
    object))


(defmethod datafy :keyframes-rule
  [schema]
  (let [object (CSSKeyframesRule. (:declaration schema) (:name schema))]
    (doseq [block (:blocks schema)]
      (.addBlock object (datafy block)))
    object))


(defmethod datafy :keyframes-block
  [schema]
  (let [object (CSSKeyframesBlock. (:selectors schema))]
    (doseq [declaration (:declarations schema)]
      (.addDeclaration object (datafy declaration)))
    object))


(defmethod datafy :media-query
  [schema]
  (let [object (CSSMediaQuery. (:medium schema))]
    (doseq [expression (:expressions schema)]
      (.addMediaExpression object (datafy expression)))
    object))


(defn to-string
  [schema]
  (let [cascading-object (CascadingStyleSheet.)
        settings-object  (CSSWriterSettings. ECSSVersion/CSS30 true)
        writer-object    (doto (CSSWriter. settings-object)
                           (.setWriteHeaderText false))]
    (doseq [node schema]
      (.addRule cascading-object (datafy node)))
    (.getCSSAsString writer-object cascading-object)))


(defn to-file
  [schema options]
  (let [cascading-object (CascadingStyleSheet.)
        settings-object  (CSSWriterSettings. ECSSVersion/CSS30 true)
        writer-object    (doto (CSSWriter. settings-object)
                           (.setWriteHeaderText false)
                           (.setContentCharset "UTF-8"))]
    (doseq [node schema]
      (.addRule cascading-object (datafy node)))
    (with-open [writer (io/writer (-> options :output-directory))]
      (.write writer (.getCSSAsString writer-object cascading-object)))))
