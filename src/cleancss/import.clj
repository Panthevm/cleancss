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
    CSSMediaExpression]))


(extend-type CSSDeclaration
  protocol/Datafiable
  (datafy [object]
    {:type       :declaration
     :property   (-> object .getProperty)
     :expression (-> object .getExpressionAsCSSString)
     :important? (-> object .isImportant)}))


(extend-type CSSStyleRule
  protocol/Datafiable
  (datafy [object]
    {:type         :style-rule
     :selectors    (->> object .getAllSelectors    (map #(.getAsCSSString %)))
     :declarations (->> object .getAllDeclarations (map protocol/datafy))}))


(extend-type CSSKeyframesRule
  protocol/Datafiable
  (datafy [object]
    {:type        :keyframes-rule
     :name        (->  object .getAnimationName)
     :declaration (->  object .getDeclaration)
     :blocks      (->> object .getAllBlocks (map protocol/datafy))}))


(extend-type CSSKeyframesBlock
  protocol/Datafiable
  (datafy [object]
    {:type         :keyframes-block
     :selectors    (->  object .getAllKeyframesSelectors)
     :declarations (->> object .getAllDeclarations (map protocol/datafy))}))


(extend-type CSSMediaRule
  protocol/Datafiable
  (datafy [object]
    {:type    :media-rule
     :queries (->> object .getAllMediaQueries (map protocol/datafy))
     :rules   (->> object .getAllRules        (map protocol/datafy))}))


(extend-type CSSMediaQuery
  protocol/Datafiable
  (datafy [object]
    {:type        :media-query
     :not         (->  object .isNot)
     :only?       (->  object .isOnly)
     :medium      (->  object .getMedium)
     :expressions (->> object .getAllMediaExpressions (map protocol/datafy))}))


(extend-type CSSMediaExpression
  protocol/Datafiable
  (datafy [object]
    {:type    :media-expression
     :feature (-> object .getFeature)
     :value   (-> object .getValue .getAsCSSString)}))


(defn from-file
  [options]
  (let [file   (-> options :input-files first io/file)
        reader (CSSReader/readFromFile file StandardCharsets/UTF_8 ECSSVersion/CSS30)]
    (map protocol/datafy (.getAllRules reader))))


(defn from-string
  [stylesheet & [options]]
  (let [reader (CSSReader/readFromString stylesheet StandardCharsets/UTF_8 ECSSVersion/CSS30)]
    (map protocol/datafy (.getAllRules reader))))
