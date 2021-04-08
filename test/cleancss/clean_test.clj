(ns cleancss.clean-test
  (:require
   [cleancss.clean  :as sut]

   [clj-ph-css.core :as    css]
   [clojure.test    :refer :all]
   [matcho.core     :refer [match]]))


(defmacro defstate
  [state before & after]
  `(match
    (->> (css/string->schema ~before)
         (sut/by-state ~state)
         (css/schema->string))
    (apply str ~@after)))


(defmacro defcontext
  [context before & after]
  `(match
    (->> (css/string->schema ~before)
         (sut/by-context ~context)
         (css/schema->string))
    (apply str ~@after)))


(defmacro match-compression
  [before & after]
  `(match
    (->> (css/string->schema ~before)
         (sut/compression)
         (css/schema->string))
    (apply str ~@after)))


(defmacro def-match-context
  [css context]
  `(match
    (->> ~css css/string->schema sut/get-context)
    ~context))


(deftest clean-by-state


  (testing "unused types"
    (defstate {:types #{"A"}}
      "A{}
       B{}"

      "A{}"))


  (testing "unused classes"
    (defstate {:classes #{"A"}}
      ".A{}
       .B{}"

      ".A{}"))


  (testing "unused identifiers"
    (defstate {:identifiers #{"A"}}
      "#A{}
       #B{}"

      "#A{}"))


  (testing "unused pseudos"
    (defstate {:pseudos #{":A"}}
      ":A{}
       :B{}"

      ":A{}"))


  (testing "unused nth-*"
    (defstate {:pseudos #{":nth-child"}}
      ":nth-child(n){}
       :nth-of-type(n){}"

      ":nth-child(n){}"))


  (testing "unused functions"
    (defstate {:functions #{":A"}}
      ":A(n){}
       :B(n){}"

      ":A(n){}"))


  (testing "unused single attribute"
    (defstate {:attributes #{["A"]}}
      "[A]{}
       [B]{}"

      "[A]{}"))


  (testing "unused = attribute"
    (defstate {:attributes #{["A" "W"]}}
      "[A=W]{}
       [A=G]{}
       [B=W]{}"

      "[A=W]{}"))


  (testing "unused ~ attribute"
    (defstate {:attributes #{["A" "C W T"]}}
      "[A~=W]{}
       [A~=G]{}
       [B~=W]{}"

      "[A~=W]{}"))


  (testing "unused ^ attribute"
    (defstate {:attributes #{["A" "WT"]}}
      "[A^=W]{}
       [A^=T]{}"

      "[A^=W]{}"))


  (testing "unused $ attribute"
    (defstate {:attributes #{["A" "WT"]}}
      "[A$=T]{}
       [A$=W]{}"

      "[A$=T]{}"))


  (testing "unused * attribute"
    (defstate {:attributes #{["A" "CWT"]}}
      "[A*=W]{}
       [A*=G]{}"

      "[A*=W]{}"))


  (testing "unused | attribute"
    (defstate {:attributes #{["A" "C-S"]
                             ["H" "C"]}}
      "[A|=C]{}
       [H|=C]{}
       [A|=S]{}
       [H|=S]{}"

      "[A|=C]{}"
      "[H|=C]{}"))


  (testing "not"
    (defstate {}
      ":not([A]){}"

      ":not([A]){}"))


  (testing "combinators"
    (defstate {:types #{"A" "B"}}
      "A B{}
       A>B{}
       A+B{}
       A~B{}
       A C{}
       A>C{}
       A+C{}
       A~C{}"

      "A B{}"
      "A>B{}"
      "A+B{}"
      "A~B{}")))


(deftest clean-by-context


  (testing "unused keyframes"
    (defcontext {:animations #{"A"}}
      "@keyframes A{}
       @keyframes B{}"

      "@keyframes A{}")))


(deftest get-context


  (testing "animation"
    (def-match-context
      "E{animation: name infinite ease-in-out}"
      {:animations #{"name"}})
    (def-match-context
      "E{animation-name: name}"
      {:animations #{"name"}})))




(deftest compression


  (testing "duplicate declarations"
    (match-compression
     "E{a:1;b:2;a:3}"

     "E{a:3;b:2}"))


  (testing "duplicate selectors"
    (match-compression
      "A,A,B{a:a}
       A A,A A{a:a}"

      "A,B{a:a}"
      "A A{a:a}"))


  (testing "duplicate style"
    (match-compression
      "E{a:1}
       E{a:2}"

      "E{a:2}"))


  (testing "duplicate media"
    (match-compression
      "@media all {A{}}
       @media all {B{}}
       A{}"

      "@media all{A{}B{}}"
      "A{}"))


  (testing "duplicate keyframes"
    (match-compression
      "@keyframes A {from {color: red}}
       @keyframes A {from {right: 100}}
       @keyframes A {to   {right: 100}}"

      "@keyframes A{from{color:red;right:100}to{right:100}}")))
