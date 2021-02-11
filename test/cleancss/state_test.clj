(ns cleancss.state-test
  (:require
   [cleancss.state :as    sut]
   [matcho.core    :refer [match]]
   [clojure.test   :refer [deftest]]))


(deftest escape
  (match (sut/escape "foo")     "foo")
  (match (sut/escape "foo:bar") "foo\\:bar")
  (match (sut/escape :foo:bar)  "foo\\:bar"))


(deftest add-identifier
  (match
   {:app.core {:identifiers #{"identifier"}}}
   (sut/add-identifier
    {}
    :app.core
    "identifier"))

  (match
   {:app.core {:identifiers #{"identifier" "identifier-2"}}}
   (sut/add-identifier
    {:app.core {:identifiers #{"identifier"}}}
    :app.core
    "identifier-2")))


(deftest add-attributes
  (match
   {:app.core {:attributes #{["hidden"] ["hreflang" "en"]}}}
   (sut/add-attributes
    {}
    :app.core
    {:hreflang "en"
     :hidden   true
     :on-click (fn [])})))


(deftest get-classes
  (match
   (sut/get-classes
    {:app.core {:classes {"class1" "a" "class2" "b" "class3" "c"}}
     :app.page {:classes {"class1" "a" "class4" "d"}}})
   {"class1" "a"
    "class2" "b"
    "class3" "c"
    "class4" "d"}))


(deftest get-identifiers
  (match
   (sut/get-identifiers
    {:app.core {:identifiers #{"identifier1"}}
     :app.page {:identifiers #{"identifier2"}}})
   #{"identifier1" "identifier2"}))


(deftest get-attributes
  (match
   (sut/get-attributes
    {:app.core {:attributes #{["attribute-name1" "attribute-value"]}}
     :app.page {:attributes #{["attribute-name2"]}}})
   #{["attribute-name2"] ["attribute-name1" "attribute-value"]}))


(deftest alias-classes
  (match
   ["a" "b"]
   (sut/alias-classes
    {:app.core {:classes {"class1" "a"}}
     :app.page {:classes {"class2" "b"}}}
    ["class1" "class2"])))


(deftest add-classes
  (match
   {:app.core {:classes {"class1" "a", "class2" "b"}}}
   (sut/add-classes {} :app.core ["class1" "class2"]))

  (match
   {:app.core {:classes {"class1" "a" "class2" "b"}}
    :app.page {:classes {"class1" "a" "class3" "c" "class4" "d"}}}
   (sut/add-classes
    {:app.core {:classes {"class1" "a", "class2" "b"}}}
    :app.page
    ["class1" "class3" "class4"])))





