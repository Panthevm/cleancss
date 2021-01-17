(ns cleancss.import
  (:require
   [clojure.core.protocols :as protocol]
   [clojure.java.io        :as io])

  (:import
   [com.helger.css.reader
    CSSReader]

   [com.helger.css
    ECSSVersion]

   [java.nio.charset
    StandardCharsets]

   [com.helger.css.decl
    CSSDeclaration
    CSSMediaRule
    CSSStyleRule
    CSSKeyframesRule
    CSSKeyframesBlock
    CSSMediaQuery
    CSSMediaExpression
    CSSSelector
    ICSSSelectorMember
    CSSSelectorSimpleMember
    ECSSSelectorCombinator
    CSSSelectorMemberNot
    CSSSelectorAttribute
    ECSSAttributeOperator
    CSSSelectorMemberFunctionLike]))


(extend-type CSSDeclaration
  protocol/Datafiable
  (datafy [object]
    {:type       :declaration
     :property   (-> object .getProperty)
     :expression (-> object .getExpressionAsCSSString)
     :important? (-> object .isImportant)}))


(extend-type CSSSelectorSimpleMember
  protocol/Datafiable
  (datafy [object]
    {:type  :selector-simple-member
     :value (.getValue object)}))


(extend-type ECSSSelectorCombinator
  protocol/Datafiable
  (datafy [object]
    {:type :selector-combinator
     :name (.getName object)}))


(extend-type CSSSelectorMemberNot
  protocol/Datafiable
  (datafy [object]
    {:type      :selector-member-not
     :selectors (->> object .getAllSelectors (map protocol/datafy))}))


(extend-type ECSSAttributeOperator
  protocol/Datafiable
  (datafy [object]
    {:type :attribute-operator
     :name (-> object .getName)}))


(extend-type CSSSelectorMemberFunctionLike
  protocol/Datafiable
  (datafy [object]
    {:type       :selector-member-function
     :name       (-> object .getFunctionName)
     :expression (-> object .getParameterExpression .getAsCSSString)}))


(extend-type CSSSelectorAttribute
  protocol/Datafiable
  (datafy [object]
    {:type      :selector-attribute
     :name      (-> object .getAttrName)
     :operator  (-> object .getOperator protocol/datafy)
     :namespace (-> object .getNamespacePrefix)
     :attribute (-> object .getAttrValue)}))


(extend-type CSSSelector
  protocol/Datafiable
  (datafy [object]
    {:type    :selector
     :members (->> object .getAllMembers (map protocol/datafy))}))


(extend-type CSSStyleRule
  protocol/Datafiable
  (datafy [object]
    {:type         :style-rule
     :selectors    (->> object .getAllSelectors    (map protocol/datafy))
     :declarations (->> object .getAllDeclarations (map protocol/datafy))}))


(extend-type CSSKeyframesBlock
  protocol/Datafiable
  (datafy [object]
    {:type         :keyframes-block
     :selectors    (->  object .getAllKeyframesSelectors)
     :declarations (->> object .getAllDeclarations (map protocol/datafy))}))


(extend-type CSSKeyframesRule
  protocol/Datafiable
  (datafy [object]
    {:type        :keyframes-rule
     :name        (->  object .getAnimationName)
     :blocks      (->> object .getAllBlocks (map protocol/datafy))
     :declaration (->  object .getDeclaration)}))


(extend-type CSSMediaExpression
  protocol/Datafiable
  (datafy [object]
    {:type    :media-expression
     :value   (-> object .getValue .getAsCSSString)
     :feature (-> object .getFeature)}))


(extend-type CSSMediaQuery
  protocol/Datafiable
  (datafy [object]
    {:type        :media-query
     :not         (->  object .isNot)
     :only?       (->  object .isOnly)
     :medium      (->  object .getMedium)
     :expressions (->> object .getAllMediaExpressions (map protocol/datafy))}))


(extend-type CSSMediaRule
  protocol/Datafiable
  (datafy [object]
    {:type    :media-rule
     :rules   (->> object .getAllRules        (map protocol/datafy))
     :queries (->> object .getAllMediaQueries (map protocol/datafy))}))


(defn from-file
  [options]
  (let [file   (-> options :input-files first io/file)
        reader (CSSReader/readFromFile file StandardCharsets/UTF_8 ECSSVersion/CSS30)]
    (map protocol/datafy (.getAllRules reader))))


(defn from-string
  [stylesheet & [options]]
  (let [reader (CSSReader/readFromString stylesheet StandardCharsets/UTF_8 ECSSVersion/CSS30)]
    (map protocol/datafy (.getAllRules reader))))


(comment
  (def s (from-file {:input-files ["/home/panthevm/study/IS/resources/public/css/tailwind.min.css"]})))
