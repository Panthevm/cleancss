(ns cleancss.export-test
  (:require
   [cleancss.export :as    sut]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho]))


(deftest style-rule
  (testing "universal-selector"
    (matcho/match
     (str "ns|*,"
          "*|*,"
          "|*,"
          "*{}")
     (sut/to-string
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
          :members [{:type :selector-simple-member :value "*"}]}]}])))


  (testing "attribute selectors"
    (matcho/match
     (str "E[foo],"
          "E[foo=\"bar\"]{}")
     (sut/to-string
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
                     :operator  {:type :attribute-operator :name "="}}]}]}])))


  (testing "structural pseudos"
    (matcho/match
     (str "E:root,"
          "E:nth-child(n),"
          "E::first-line,"
          "E:lang(fr),"
          "E:not([F]){}")
     (sut/to-string
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
                                  :members [{:type :selector-attribute :name "F"}]}]}]}]}])))


  (testing "class html"
    (matcho/match
     (str "*.E,"
          ".E,"
          "E.F{}")
     (sut/to-string
      [{:type :style-rule
        :selectors
        [{:type    :selector
          :members [{:type :selector-simple-member :value "*"}
                    {:type :selector-simple-member :value ".E"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value ".E"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value "E"}
                    {:type :selector-simple-member :value ".F"}]}]}])))


  (testing "id selectors"
    (matcho/match
     (str "#E,"
          "*#E,"
          "E#F{}")
     (sut/to-string
      [{:type :style-rule
        :selectors
        [{:type    :selector
          :members [{:type :selector-simple-member :value "#E"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value "*"}
                    {:type :selector-simple-member :value "#E"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value "E"}
                    {:type :selector-simple-member :value "#F"}]}]}])))


  (testing "combinators"
    (matcho/match
     (str "E F,"
          "E+F{}")
     (sut/to-string
      [{:type :style-rule
        :selectors
        [{:type    :selector
          :members [{:type :selector-simple-member :value "E"}
                    {:type :selector-combinator    :name  " "}
                    {:type :selector-simple-member :value "F"}]}
         {:type    :selector
          :members [{:type :selector-simple-member :value "E"}
                    {:type :selector-combinator    :name  "+"}
                    {:type :selector-simple-member :value "F"}]}]}]))))


(deftest media-rule
  (matcho/match
   (str "@media screen{}"
        "@media (min-width:500px){}"
        "@media not screen and (min-width:500px){}"
        "@media only handheld and (min-width:500px) and (max-width:15em){}")
   (sut/to-string
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
                                :value   "15em"}]}]}])))


(deftest keyframes-rule
  (matcho/match
   "@keyframes foo{from{}50%{}20%,80%{}to{}}"
   (sut/to-string
    [{:type        :keyframes-rule
      :name        "foo"
      :declaration "@keyframes"
      :blocks
      [{:type :keyframes-block :selectors ["from"]}
       {:type :keyframes-block :selectors ["50%"]}
       {:type :keyframes-block :selectors ["20%" "80%"]}
       {:type :keyframes-block :selectors ["to"]}]}])))


(deftest declarations
  (testing "style rule"
    (matcho/match
     "E{color:red !important}"
     (sut/to-string
      [{:type         :style-rule
        :selectors    [{:type    :selector
                        :members [{:type :selector-simple-member :value "E"}]}]
        :declarations [{:type       :declaration
                        :property   "color"
                        :expression "red"
                        :important? true}]}]) ))

  (testing "media rule"
    (matcho/match
     "@media screen{E{color:red}}"
     (sut/to-string
      [{:type    :media-rule
        :queries [{:type   :media-query
                   :not    false
                   :only?  false
                   :medium "screen"}]
        :rules   [{:type         :style-rule
                   :selectors    [{:type    :selector
                                   :members [{:type :selector-simple-member :value "E"}]}]
                   :declarations [{:type       :declaration
                                   :property   "color"
                                   :expression "red"
                                   :important? false}]}]}]) ))

  (testing "keyframes"
    (matcho/match
     "@keyframes foo{from{color:red}}"
     (sut/to-string
      [{:type        :keyframes-rule
        :name        "foo"
        :declaration "@keyframes"
        :blocks      [{:type         :keyframes-block
                       :selectors    ["from"]
                       :declarations [{:type       :declaration
                                       :property   "color"
                                       :expression "red"
                                       :important? false}]}]}]) )))
