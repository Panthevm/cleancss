(ns cleancss.data-test
  (:require
   [cleancss.data :as sut]
   [clojure.test  :refer :all]
   [matcho.core   :as matcho]))


(defmacro deftransform-match
  [& params]
  (let [stylesheet
        (apply str (drop-last params))
        schema
        (last params)]
    `(do
       (testing "schema"
         (matcho/match ~schema (sut/string->schema ~stylesheet)))
       (testing "object"
         (matcho/match ~stylesheet (sut/schema->string ~schema))))))


(deftest style-rule

  (testing "universal selector"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#universal-selector
    (deftransform-match
      "ns|*,"
      "*|*,"
      "|*,"
      "*"
      "{}"
      [{:selectors
        [{:members
          [{:group :type :value "ns|" :type :member}
           {:group :type :value "*"   :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "*|" :type :member}
           {:group :type :value "*"  :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "|" :type :member}
           {:group :type :value "*" :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "*" :type :member}]
          :type    :selector}]
        :declarations []
        :type         :style-rule}]))


  (testing "type selector"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#type-selectors
    (deftransform-match
      "A,"
      "|A,"
      "*|A,"
      "ns|A"
      "{}"
      [{:type :style-rule
        :selectors
        [{:members
          [{:group :type :value "A" :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "|" :type :member}
           {:group :type :value "A" :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "*|" :type :member}
           {:group :type :value "A"  :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "ns|" :type :member}
           {:group :type :value "A"  :type :member}]
          :type :selector}]
        :declarations []}]))


  (testing "attribute selectors"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#attribute-selectors
    (deftransform-match
      "[B],"
      "[B=C],"
      "[B~=C],"
      "[B^=C],"
      "[B$=C],"
      "[B*=C],"
      "[B|=C]"
      "{}"
      [{:type         :style-rule
        :selectors
        [{:members
          [{:name "B" :type :selector-attribute}]
          :type :selector}
         {:members
          [{:name      "B"
            :operator  {:name "=" :type :attribute-operator}
            :attribute "C"
            :type      :selector-attribute}]
          :type :selector}
         {:members
          [{:name      "B"
            :operator  {:name "~=" :type :attribute-operator}
            :attribute "C"
            :type      :selector-attribute}]
          :type :selector}
         {:members
          [{:name      "B"
            :operator  {:name "^=" :type :attribute-operator}
            :attribute "C"
            :type      :selector-attribute}]
          :type :selector}
         {:members
          [{:name      "B"
            :operator  {:name "$=" :type :attribute-operator}
            :attribute "C"
            :type      :selector-attribute}]
          :type :selector}
         {:members
          [{:name      "B"
            :operator  {:name "*=" :type :attribute-operator}
            :attribute "C"
            :type      :selector-attribute}]
          :type :selector}
         {:members
          [{:name      "B"
            :operator  {:name "|=" :type :attribute-operator}
            :attribute "C"
            :type      :selector-attribute}]
          :type :selector}]
        :declarations []}]))


  (testing "pseudo-classes"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#structural-pseudos
    (deftransform-match
      ":root,"
      "::after"
      "{}"
      [{:type :style-rule
        :selectors
        [{:members
          [{:group :pseudo :value ":root" :type :member}]
          :type :selector}
         {:members
          [{:group :pseudo :value "::after" :type :member}]
          :type :selector}]
        :declarations []}]))


  (testing "classes"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#class-html
    (deftransform-match
      ".A,"
      "*.A,"
      "A.B"
      "{}"
      [{:type :style-rule
        :selectors
        [{:members
          [{:group :class :value ".A" :type :member}]
          :type :selector}
         {:members
          [{:group :type  :value "*"  :type :member}
           {:group :class :value ".A" :type :member}]
          :type :selector}
         {:members
          [{:group :type  :value "A"  :type :member}
           {:group :class :value ".B" :type :member}]
          :type :selector}]
        :declarations []}]))


  (testing "id selectors"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#id-selectors
    (deftransform-match
      "#A,"
      "A#B,"
      "*#A"
      "{}"
      [{:type :style-rule
        :selectors
        [{:members
          [{:group :identifier :value "#A" :type :member}]
          :type :selector}
         {:members
          [{:group :type       :value "A"  :type :member}
           {:group :identifier :value "#B" :type :member}]
          :type :selector}
         {:members
          [{:group :type       :value "*"  :type :member}
           {:group :identifier :value "#A" :type :member}]
          :type :selector}]
        :declarations []}]))


  (testing "negation"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#negation
    (deftransform-match
      ":not(A),"
      "A:not(B),"
      "A|B:not(C):not(D)"
      "{}"
      [{:type :style-rule
        :selectors
        [{:members
          [{:selectors
            [{:members
              [{:group :type :value "A" :type :member}]
              :type :selector}]
            :type :selector-member-not}]
          :type :selector}
         {:members
          [{:group :type :value "A" :type :member}
           {:selectors
            [{:members
              [{:group :type :value "B" :type :member}]
              :type :selector}]
            :type :selector-member-not}]
          :type :selector}
         {:members
          [{:group :type :value "A|" :type :member}
           {:group :type :value "B"  :type :member}
           {:selectors
            [{:members
              [{:group :type :value "C" :type :member}]
              :type :selector}]
            :type :selector-member-not}
           {:selectors
            [{:members
              [{:group :type :value "D" :type :member}]
              :type :selector}]
            :type :selector-member-not}]
          :type :selector}]
        :declarations []}]))


  (testing "combinators"
    (deftransform-match
      "A B,"
      "A>B,"
      "A+B,"
      "A~B"
      "{}"
      [{:type :style-rule
        :selectors
        [{:members
          [{:group :type :value "A"  :type :member}
           {:name  " "  :type :selector-combinator}
           {:group :type :value "B"  :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "A"  :type :member}
           {:name  ">"  :type :selector-combinator}
           {:group :type :value "B"  :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "A" :type :member}
           {:name  "+" :type :selector-combinator}
           {:group :type :value "B" :type :member}]
          :type :selector}
         {:members
          [{:group :type :value "A" :type :member}
           {:name  "~" :type :selector-combinator}
           {:group :type :value "B" :type :member}]
          :type :selector}]
        :declarations []}]))


  (testing "functions"
    (deftransform-match
      ":lang(en){}"
      [{:selectors
        [{:members
          [{:name ":lang(" :expression "en" :type :member-function}]
          :type :selector}]
        :declarations []
        :type :style-rule}])))



(deftest media-rule
  ;; https://www.w3.org/TR/css3-mediaqueries/


  (testing "query"
    (deftransform-match
      "@media screen{}"
      "@media not screen{}"
      "@media only screen{}"
      [{:rules []
        :queries
        [{:not? false
          :only? false
          :medium "screen"
          :expressions []
          :type :media-query}]
        :type :media-rule}
       {:rules []
        :queries
        [{:not? true
          :only? false
          :medium "screen"
          :expressions []
          :type :media-query}]
        :type :media-rule}
       {:rules []
        :queries
        [{:not? false
          :only? true
          :medium "screen"
          :expressions []
          :type :media-query}]
        :type :media-rule}]))


  (testing "expression"
    (deftransform-match
      "@media (min-width:100px){}"
      [{:rules []
        :queries
        [{:not?  false
          :only? false
          :expressions
          [{:value "100px"
            :feature "min-width"
            :type :media-expression}]
          :type :media-query}]
        :type :media-rule}]))


  (testing "rules"
    (deftransform-match
      "@media screen{A{}}"
      "@media screen{@media screen{A{}}}"
      [{:rules
        [{:type :style-rule
          :selectors
          [{:members
            [{:group :type :value "A" :type :member}],
            :type :selector}]
          :declarations
          []}]
        :queries
        [{:not? false
          :only? false
          :medium "screen"
          :expressions
          []
          :type :media-query}]
        :type :media-rule}
       {:rules
        [{:rules
          [{:type :style-rule
            :selectors
            [{:members
              [{:group :type :value "A" :type :member}]
              :type :selector}]
            :declarations
            []}]
          :queries
          [{:not? false
            :only? false
            :medium "screen"
            :expressions
            []
            :type :media-query}]
          :type :media-rule}]
        :queries
        [{:not? false
          :only? false
          :medium "screen"
          :expressions
          []
          :type :media-query}]
        :type :media-rule}])))



(deftest keyframes-rule
  ;; https://www.w3.org/TR/css-animations-1/
  (deftransform-match
    "@keyframes foo{from{}50%{}20%,80%{}to{}}"
    [{:declaration "@keyframes"
      :name "foo"
      :blocks
      [{:type :keyframes-block :selectors ["from"] :declarations []}
       {:type :keyframes-block :selectors ["50%"] :declarations []}
       {:type :keyframes-block
        :selectors ["20%" "80%"]
        :declarations []}
       {:type :keyframes-block :selectors ["to"] :declarations []}]
      :type :keyframes-rule}]))
