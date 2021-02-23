(ns cleancss.import-test
  (:require
   [cleancss.import :as    sut]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho]))


(deftest style-rule
  (testing "universal-selector"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#universal-selector
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#type-selectors
    (matcho/match
     (sut/from-string
      "ns|*, *|*, |*, * {}")
     [{:type :style-rule
       :selectors
       [{:type    :selector
         :members [{:type :selector-simple-member :value "ns|"}
                   {:type :selector-simple-member :value "*"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "*|"}
                   {:type :selector-simple-member :value "*"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "|"}
                   {:type :selector-simple-member :value "*"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "*"}]}]}]))


  (testing "attribute selectors"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#attribute-selectors
    (matcho/match
     (sut/from-string
      "E[foo],
       E[foo=\"bar\"] {}")
     [{:type :style-rule
       :selectors
       [{:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type :selector-attribute :name "foo"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type      :selector-attribute
                    :name      "foo"
                    :attribute "\"bar\""
                    :operator  {:type :attribute-operator :name "="}}]}]}]))


  (testing "structural pseudos"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#structural-pseudos
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#link
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#useraction-pseudos
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#target-pseudo
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#lang-pseudo
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#UIstates
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#first-line
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#gen-content
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#negation
    (matcho/match
     (sut/from-string
      "E:root,
       E:nth-child(n),
       E::first-line,
       E:lang(fr),
       E:not([F]) {}")
     [{:type :style-rule
       :selectors
       [{:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type :selector-simple-member :value ":root"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type :selector-simple-member :value ":nth-child(n)"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type :selector-simple-member :value "::first-line"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type       :selector-member-function
                    :name       ":lang("
                    :expression "fr"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type      :selector-member-not
                    :selectors [{:type    :selector
                                 :members [{:type :selector-attribute :name "F"}]}]}]}]}]))


  (testing "class html"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#class-html
    (matcho/match
     (sut/from-string
      "*.E,
       .E,
       E.F {}")
     [{:type :style-rule
       :selectors
       [{:type    :selector
         :members [{:type :selector-simple-member :value "*"}
                   {:type :selector-simple-member :value ".E"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value ".E"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value "E"}
                    {:type :selector-simple-member :value ".F"}]}]}]))


  (testing "id selectors"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#id-selectors
    (matcho/match
     (sut/from-string
      "#E,
       *#E,
       E#F {}")
     [{:type :style-rule
       :selectors
       [{:type    :selector
         :members [{:type :selector-simple-member :value "#E"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "*"}
                   {:type :selector-simple-member :value "#E"}]}
        {:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type :selector-simple-member :value "#F"}]}]}]))


  (testing "combinators"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#descendant-combinators
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#child-combinators
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#adjacent-sibling-combinators
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#general-sibling-combinators
    (matcho/match
     (sut/from-string
      "E F,
       E + F {}")
     [{:type :style-rule
       :selectors
       [{:type    :selector
         :members [{:type :selector-simple-member :value "E"}
                   {:type :selector-combinator    :name  " "}
                   {:type :selector-simple-member :value "F"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value "E"}
                    {:type :selector-combinator    :name  "+"}
                    {:type :selector-simple-member :value "F"}]}]}])))


(deftest media-rule
  ;; https://www.w3.org/TR/css3-mediaqueries/
  (matcho/match
   (sut/from-string
    "@media screen {}
     @media (min-width:500px) {}
     @media not screen and (min-width:500px) {}
     @media only handheld and (min-width:500px) and (max-width: 15em) {}")
   [{:type    :media-rule
     :queries [{:type   :media-query
                :not    false
                :only?  false
                :medium "screen"}]}
    {:type    :media-rule
     :queries [{:type        :media-query
                :not         false
                :only?       false
                :medium      nil
                :expressions [{:type    :media-expression
                               :feature "min-width"
                               :value   "500px"}]}]}
    {:type    :media-rule
     :queries [{:type        :media-query
                :not         true
                :only?       false
                :medium      "screen"
                :expressions [{:type    :media-expression
                               :feature "min-width"
                               :value   "500px"}]}]}
    {:type    :media-rule
     :queries [{:type        :media-query
                :not         false
                :only?       true
                :medium      "handheld"
                :expressions [{:type    :media-expression
                               :feature "min-width"
                               :value   "500px"}
                              {:type    :media-expression
                               :feature "max-width"
                               :value   "15em"}]}]}])

  (testing "rules"
    (matcho/match
     (sut/from-string
      "@media screen {
         E {}
         @media handheld {}
       }")
     [{:type  :media-rule
       :rules [{:type      :style-rule
                :selectors [{:type    :selector
                             :members [{:type :selector-simple-member :value "E"}]}]}
               {:type    :media-rule
                :queries [{:type   :media-query
                           :not    false
                           :only?  false
                           :medium "handheld"}]
                :rules   []}]}])))


(deftest keyframes-rule
  ;; https://www.w3.org/TR/css-animations-1/
  (matcho/match
   (sut/from-string
    "@keyframes foo {from {} 50% {} 20%,80% {} to {}}")
   [{:type        :keyframes-rule
     :name        "foo"
     :declaration "@keyframes"
     :blocks
     [{:type :keyframes-block :selectors ["from"]}
      {:type :keyframes-block :selectors ["50%"]}
      {:type :keyframes-block :selectors ["20%" "80%"]}
      {:type :keyframes-block :selectors ["to"]}]}]))


(deftest declarations
  ;; https://www.w3.org/TR/css-cascade-3/
  (testing "style rule"
    (matcho/match
     (sut/from-string "E {color: red !important}")
     [{:type         :style-rule
       :declarations [{:type       :declaration
                       :property   "color"
                       :expression "red"
                       :important? true}]}]))

  (testing "media rule"
    (matcho/match
     (sut/from-string "@media screen {E {color: red}}")
     [{:type  :media-rule
       :rules [{:type         :style-rule
                :declarations [{:type       :declaration
                                :property   "color"
                                :expression "red"
                                :important? false}]}]}]))

  (testing "keyframes"
    (matcho/match
     (sut/from-string "@keyframes foo {from {color: red}}")
     [{:type   :keyframes-rule
       :blocks [{:type         :keyframes-block
                 :declarations [{:type       :declaration
                                 :property   "color"
                                 :expression "red"
                                 :important? false}]}]}])))
