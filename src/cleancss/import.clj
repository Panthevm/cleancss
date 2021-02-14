(ns cleancss.import
  (:require
   [clojure.core.protocols :as protocol]
   [clojure.java.io        :as io]
   [clojure.string         :as string])

  (:import
   [com.helger.css.reader
    CSSReader]

   [com.helger.css
    ECSSVersion]

   [java.nio.charset
    StandardCharsets]

   [com.helger.css.decl
    CSSStyleRule
    CSSDeclaration

    CSSMediaRule
    CSSMediaQuery
    CSSMediaExpression

    CSSKeyframesRule
    CSSKeyframesBlock

    CSSSelector
    ICSSSelectorMember
    CSSSelectorMemberNot
    CSSSelectorAttribute
    ECSSAttributeOperator
    ECSSSelectorCombinator
    CSSSelectorSimpleMember
    CSSSelectorMemberFunctionLike]))


(extend-type CSSDeclaration
  protocol/Datafiable
  (datafy [object]
    (let [property   (-> object .getProperty)
          expression (-> object .getExpressionAsCSSString)
          meta-type  (cond
                       (contains? #{"animation" "animation-name"} property)
                       :animation

                       (string/starts-with? property "--")
                       :variable)]
      {:type       :declaration
       :property   property
       :expression expression
       :important? (-> object .isImportant)
       :meta       {:type      meta-type
                    :variables (when (string/includes? expression "var(")
                                 (re-seq #"(?<=var\()(?:.*?)(?=\))" expression))
                    :animation (when (= :animation meta-type)
                                 (re-find #"\S+" expression))}})))


(extend-type CSSSelectorSimpleMember
  protocol/Datafiable
  (datafy [object]
    (let [value (.getValue object)
          group (cond
                  (string/starts-with? value ".") :class
                  (string/starts-with? value "#") :identifier
                  (string/starts-with? value ":") :pseudo
                  :else                           :type)]
      {:type  :selector-simple-member
       :value value
       :group group
       :name  (cond
                (= :pseudo group) (-> value (string/split #"\(") first)
                (= :type   group) value
                :else             (subs value 1))})))


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
    (let [function (-> object .getFunctionName)]
      {:type       :selector-member-function
       :name       function
       :function   (->> function drop-last (apply str))
       :expression (-> object .getParameterExpression .getAsCSSString)})))


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
  (->>
   options :input-files
   (mapcat
    (fn [directory]
      (map protocol/datafy
           (.getAllRules (CSSReader/readFromFile (io/file directory) StandardCharsets/UTF_8 ECSSVersion/CSS30)))))))


(defn from-string
  [stylesheet & [options]]
  (let [reader (CSSReader/readFromString stylesheet StandardCharsets/UTF_8 ECSSVersion/CSS30)]
    (mapv protocol/datafy (.getAllRules reader))))
