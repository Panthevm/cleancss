(ns cleancss.filter-test
  (:require
   [cleancss.filter :as    sut]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho]))


(deftest used-member?


  (testing "type selectors"
    ;; https://www.w3.org/TR/selectors-3/#type-selectors

    (testing "ns|"
      (def member
        {:type :selector-simple-member :value "foo|"})

      (is      (sut/used-member? {:namespaces #{"foo"}} member))
      (is (not (sut/used-member? {:namespaces #{"bar"}} member))))


    (testing "|"
      (def member
        {:type :selector-simple-member :value "|"})

      (is (sut/used-member? {:types #{"|"}} member)))


    (testing "E"
      (def member
        {:type :selector-simple-member :value "h1"})

      (is      (sut/used-member? {:types #{"h1"}} member))
      (is (not (sut/used-member? {:types #{"h2"}} member)))))


  (testing "attribute selectors"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#attribute-selectors

    (testing "[foo]"
      (def member
        {:type :selector-attribute :name "foo"})

      (is      (sut/used-member? {:attributes #{["foo"]}} member))
      (is (not (sut/used-member? {:attributes #{["bar"]}} member))))


    (testing "[foo`<operator>`bar]"


      (testing "operator ="
        (def member
          {:type      :selector-attribute
           :name      "hreflang"
           :attribute "fr"
           :operator  {:type :attribute-operator :name "="}})

        (is      (sut/used-member? {:attributes #{["hreflang" "fr"]}} member))
        (is (not (sut/used-member? {:attributes #{["hreflang" "en"]}} member)))
        (is (not (sut/used-member? {:attributes #{["class"    "fr"]}} member))))


      (testing "operator ~"
        (def member
          {:type      :selector-attribute
           :name      "hreflang"
           :attribute "fr"
           :operator  {:type :attribute-operator :name "~"}})

        (is      (sut/used-member? {:attributes #{["hreflang" "en fr"]}} member))
        (is (not (sut/used-member? {:attributes #{["hreflang" "en ru"]}} member))))


      (testing "operator |"
        (def member
          {:type      :selector-attribute
           :name      "hreflang"
           :attribute "en"
           :operator  {:type :attribute-operator :name "|"}})

        (is      (sut/used-member? {:attributes #{["hreflang" "en-us"]}} member))
        (is      (sut/used-member? {:attributes #{["hreflang" "en"]}}    member))
        (is (not (sut/used-member? {:attributes #{["hreflang" "ru"]}}    member))))


      (testing "operator ^"
        (def member
          {:type      :selector-attribute
           :name      "src"
           :attribute "image/"
           :operator  {:type :attribute-operator :name "^"}})

        (is      (sut/used-member? {:attributes #{["src" "image/photo.png"]}} member))
        (is (not (sut/used-member? {:attributes #{["src" "video/photo.png"]}} member))))


      (testing "operator $"
        (def member
          {:type      :selector-attribute
           :name      "src"
           :attribute ".png"
           :operator  {:type :attribute-operator :name "$"}})

        (is      (sut/used-member? {:attributes #{["src" "image/photo.png"]}} member))
        (is (not (sut/used-member? {:attributes #{["src" "image/photo.jpg"]}} member))))


      (testing "operator *"
        (def member
          {:type      :selector-attribute
           :name      "src"
           :attribute "photo"
           :operator  {:type :attribute-operator :name "*"}})

        (is      (sut/used-member? {:attributes #{["src" "image/photo.png"]}} member))
        (is (not (sut/used-member? {:attributes #{["src" "image/memes.jpg"]}} member))))))


  (testing "pseudo"


    (testing "structural"
      ;; https://www.w3.org/TR/selectors-3/#structural-pseudos
      (def member
        {:type :selector-simple-member :value "::before"})

      (is      (sut/used-member? {:pseudos #{"::before"}} member))
      (is (not (sut/used-member? {:pseudos #{"::after"}}  member))))


    (testing "negation"
      ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#negation
      (def member
        {:type      :selector-member-not
         :selectors [{:type :selector
                      :members [{:type :selector-attribute
                                 :name "disabled"}]}]})

      (is      (sut/used-member? {:attributes #{["hidden"]}}   member))
      (is (not (sut/used-member? {:attributes #{["disabled"]}} member)))))


  (testing "#id"
    ;; https://www.w3.org/TR/2018/REC-selectors-3-20181106/#id-selectors
    (def member
      {:type :selector-simple-member :value "#id"})

    (is      (sut/used-member? {:identifiers #{"id"}}   member))
    (is (not (sut/used-member? {:identifiers #{"save"}} member))))


  (testing "combinators"
    ;; https://www.w3.org/TR/selectors-3/#descendant-combinators
    (is (sut/used-member? {} {:type :selector-combinator :name " "}))
    (is (sut/used-member? {} {:type :selector-combinator :name ">"}))
    (is (sut/used-member? {} {:type :selector-combinator :name "+"}))
    (is (sut/used-member? {} {:type :selector-combinator :name "~"})))


  (testing "class html"
    ;; https://www.w3.org/TR/selectors-3/#class-html
    (def member
      {:type :selector-simple-member :value ".name"})

    (is      (sut/used-member? {:classes #{"name"}} member))
    (is (not (sut/used-member? {:classes #{"home"}} member)))))


(deftest make-clean
  (testing "selectors"
    (testing "style rule"
      (matcho/match
       (sut/make-clean
        {:namespaces  #{}
         :types       #{"h1"}
         :identifiers #{}
         :classes     #{}
         :pseudos     #{}
         :functions   #{}
         :attributes  #{}}
        [{:type :style-rule
            :selectors
            [{:type :selector :members [{:type :selector-simple-member :value "h1"}]}
             {:type :selector :members [{:type :selector-simple-member :value "iframe"}]}]}
           {:type :style-rule
            :selectors
            [{:type :selector :members [{:type :selector-simple-member :value "iframe"}]}]}])
       [{:type      :style-rule
         :selectors [{:type    :selector
                      :members [{:type :selector-simple-member :value "h1"}]}]}]))


    (testing "media rule"
      (matcho/match
       (sut/make-clean
        {:types #{"h1"}}
        [{:type  :media-rule
          :rules [{:type      :style-rule
                   :selectors [{:type    :selector
                                :members [{:type :selector-simple-member :value "h1"}]}
                               {:type    :selector
                                :members [{:type :selector-simple-member :value "h2"}]}]}]}
         {:type  :media-rule
          :rules [{:type      :style-rule
                   :selectors [{:type    :selector
                                :members [{:type :selector-simple-member :value "h2"}]}]}]}])
       [{:type  :media-rule
         :rules [{:type      :style-rule
                  :selectors [{:type    :selector
                               :members [{:type :selector-simple-member :value "h1"}]}]}]}]))


    (testing "keyframes-rule"
      (matcho/match
       (sut/make-clean
        {:keyframes? true
         :types      #{"h1"}}
        [{:type  :media-rule
          :rules [{:type         :style-rule
                   :selectors    [{:type    :selector
                                   :members [{:type :selector-simple-member :value "h1"}]}]
                   :declarations [{:type       :declaration
                                   :property   "animation"
                                   :expression "spin 1s linear infinite"
                                   :important? false}]}]}
         {:type :keyframes-rule :name "spin"}
         {:type :keyframes-rule :name "ping"}])
       [{:type :keyframes-rule :name "spin"}
        {:type  :media-rule
         :rules [{:type      :style-rule
                  :selectors [{:type    :selector
                               :members [{:type :selector-simple-member :value "h1"}]}]}]}]))))
