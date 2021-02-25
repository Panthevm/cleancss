(ns cleancss.filter-test
  (:require
   [cleancss.filter  :as sut]
   [cleancss.import  :as import]
   [cleancss.export  :as export]
   [clojure.test     :refer :all]
   [matcho.core      :refer [match]]))


(defmacro defstate
  [state before & after]
  `(match
    (->> (import/from-string ~before)
         (sut/by-state ~state)
         (export/to-string))
    (apply str ~@after)))


(defmacro defcontext
  [context before & after]
  `(match
    (->> (import/from-string ~before)
         (sut/by-context ~context)
         (export/to-string))
    (apply str ~@after)))


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
    (defstate {:attributes #{["A"]}}
      ":not([B]){}
       :not([A]){}"

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

      "@keyframes A{}"))

  (testing "unused variables"
    (defcontext {:variables      #{"--a"}
                 :used-variables #{"--a"}} 
      "E{--a:0;--b:1}"

      "E{--a:0}"))


  (testing "empty reference"
    (defcontext {:variables #{"--a"}} 
      "E{color:var(--a);
         right:var(--b)}"

      "E{color:var(--a)}"))
  

  (testing "empty"
    (defcontext {}
      "A{}
       @media all{}
       @media all{A{}}
       @media all{@media all{}}"

      "")))
