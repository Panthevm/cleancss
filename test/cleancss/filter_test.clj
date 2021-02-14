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
         (sut/clean-by-state ~state)
         (export/to-string))
    (apply str ~@after)))


(defmacro defcontext
  [context before & after]
  `(match
    (->> (import/from-string ~before)
         (sut/clean-by-context ~context)
         (export/to-string))
    (apply str ~@after)))


(deftest clean-by-state


  (testing "unused types"
    (defstate {:types #{"A"}}
      "A{o:o}
       B{o:o}"

      "A{o:o}"))


  (testing "unused classes"
    (defstate {:classes #{"A"}}
      ".A{o:o}
       .B{o:o}"

      ".A{o:o}"))


  (testing "unused identifiers"
    (defstate {:identifiers #{"A"}}
      "#A{o:o}
       #B{o:o}"

      "#A{o:o}"))


  (testing "unused pseudos"
    (defstate {:pseudos #{":A"}}
      ":A{o:o}
       :B{o:o}"

      ":A{o:o}"))


  (testing "unused nth-*"
    (defstate {:pseudos #{":nth-child"}}
      ":nth-child(n){o:o}
       :nth-of-type(n){o:o}"

      ":nth-child(n){o:o}"))


  (testing "unused functions"
    (defstate {:functions #{":A"}}
      ":A(n){o:o}
       :B(n){o:o}"

      ":A(n){o:o}"))


  (testing "unused single attribute"
    (defstate {:attributes #{["A"]}}
      "[A]{o:o}
       [B]{o:o}"

      "[A]{o:o}"))


  (testing "unused = attribute"
    (defstate {:attributes #{["A" "W"]}}
      "[A=W]{o:o}
       [A=G]{o:o}
       [B=W]{o:o}"

      "[A=W]{o:o}"))


  (testing "unused ~ attribute"
    (defstate {:attributes #{["A" "C W T"]}}
      "[A~=W]{o:o}
       [A~=G]{o:o}
       [B~=W]{o:o}"

      "[A~=W]{o:o}"))


  (testing "unused ^ attribute"
    (defstate {:attributes #{["A" "WT"]}}
      "[A^=W]{o:o}
       [A^=T]{o:o}"

      "[A^=W]{o:o}"))


  (testing "unused $ attribute"
    (defstate {:attributes #{["A" "WT"]}}
      "[A$=T]{o:o}
       [A$=W]{o:o}"

      "[A$=T]{o:o}"))


  (testing "unused * attribute"
    (defstate {:attributes #{["A" "CWT"]}}
      "[A*=W]{o:o}
       [A*=G]{o:o}"

      "[A*=W]{o:o}"))


  (testing "unused | attribute"
    (defstate {:attributes #{["A" "C-S"]
                             ["H" "C"]}}
      "[A|=C]{o:o}
       [H|=C]{o:o}
       [A|=S]{o:o}
       [H|=S]{o:o}"

      "[A|=C]{o:o}"
      "[H|=C]{o:o}"))


  (testing "not"
    (defstate {:attributes #{["A"]}}
      ":not([B]){o:o}
       :not([A]){o:o}"

      ":not([A]){o:o}"))


  (testing "combinators"
    (defstate {:types #{"A" "B"}}
      "A B{o:o}
       A>B{o:o}
       A+B{o:o}
       A~B{o:o}
       A C{o:o}
       A>C{o:o}
       A+C{o:o}
       A~C{o:o}"

      "A B{o:o}"
      "A>B{o:o}"
      "A+B{o:o}"
      "A~B{o:o}")))


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
