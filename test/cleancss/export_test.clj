(ns cleancss.export-test
  (:require
   [cleancss.export :as    sut]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho]))


(deftest import-test


  (testing "style rule"
    (matcho/match
     (sut/to-string
      [{:type      :style-rule
        :selectors ["body"]
        :declarations
        [{:type       :declaration
          :property   "color"
          :expression "red"
          :important? true}
         {:type       :declaration
          :property   "margin"
          :expression "0px"
          :important? false}]}])
     (str "body{"
            "color:red !important;"
            "margin:0"
          "}")))


  (testing "keyframes rule"
    (matcho/match
     (sut/to-string
      [{:type        :keyframes-rule
        :name        "ping"
        :declaration "@keyframes"
        :blocks
        [{:type      :keyframes-block
          :selectors ["75%" "100%"]
          :declarations
          [{:type       :declaration
            :property   "transform"
            :expression "scale(2)"
            :important? false}
           {:type       :declaration
            :property   "opacity"
            :expression "0"
            :important? false}]}]}])
     (str "@keyframes ping{"
            "75%,100%{"
              "transform:scale(2);"
              "opacity:0"
            "}"
          "}")))


  (testing "media rule"
    (matcho/match
     (sut/to-string
      [{:type :media-rule
        :queries
        [{:type :media-query
          :only? false
          :not false
          :medium nil
          :expressions
          [{:type :media-expression
            :feature "min-width"
            :value  "640px"}]}]
        :rules [{:type :style-rule
                 :selectors [".sm:container"]
                 :declarations [{:type :declaration
                                 :property "width"
                                 :expression "100%"
                                 :important? false}]}
                {:type :media-rule
                 :queries [{:type :media-query
                            :only? false
                            :not false
                            :expressions [{:type :media-expression
                                           :feature "min-width"
                                           :value "640px"}]}]
                 :rules
                 [{:type :style-rule
                   :selectors [".sm:container"]
                   :declarations [{:type :declaration
                                   :property "max-width"
                                   :expression "640px"
                                   :important? false}]}]}]}])
     (str "@media (min-width:640px){"
            ".sm:container{"
              "width:100%"
            "}"
            "@media (min-width:640px){"
               ".sm:container{"
                 "max-width:640px"
               "}"
            "}"
          "}"))))
