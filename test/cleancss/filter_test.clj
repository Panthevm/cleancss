(ns cleancss.filter-test
  (:require
   [cleancss.filter :as    sut]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho]))


(deftest used-selector?
  (is (sut/used-selector?      #{"foo"}  "foo"))
  (is (not (sut/used-selector? #{".foo"} "foo")))

  (is (sut/used-selector? #{"foo" "bar"} "foo bar"))
  ;; TODO (is (not (sut/used-selector? #{"foo"} "foo bar")))

  (is (sut/used-selector? #{"foo" "bar"} "foo > bar"))
  ;; TODO (is (not (sut/used-selector? #{".foo"} ".foo > .bar")))
  )


(deftest remove-unused-styles
  (matcho/match
   (sut/remove-unused-stylesheets
    #{".foo"}
    [{:type :style-rule :selectors ["::after"]}
     {:type :style-rule :selectors ["body"]}
     {:type :style-rule :selectors [".foo" ".zaz"]}])
   [{:type :style-rule :selectors [".foo"]}])

  (matcho/match
   (sut/remove-unused-stylesheets
    #{".foo"}
    [{:type :style-rule :selectors [".foo"]}
     {:type :media-rule
      :rules [{:type :style-rule :selectors [".not"]}]}
     {:type :media-rule
      :rules [{:type :style-rule :selectors [".foo"]}
              {:type :media-rule
               :rules [{:type :style-rule :selectors [".not"]}]}]}])
   [{:type :style-rule :selectors [".foo"]}
    {:type :media-rule
     :rules [{:type :style-rule :selectors [".foo"]}]}]))
