(ns cleancss.filter-test
  (:require
   [cleancss.filter :as    sut]
   [cleancss.core   :as    core]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho :refer [match]]))


(defmacro defcase
  [application after & before]
  `(match (->> ~after
               (core/import-from-string)
               (sut/make-clean ~application)
               (core/export-to-string))
          (apply str ~@before)))


(deftest style-rule


  (testing "unused types"
    (defcase {:types #{"A"}}
      "A{o:o}
       B{o:o}"

      "A{o:o}"))


  (testing "unused classes"
    (defcase {:classes #{"A"}}
      ".A{o:o}
       .B{o:o}"

      ".A{o:o}"))


  (testing "unused identifiers"
    (defcase {:identifiers #{"A"}}
      "#A{o:o}
       #B{o:o}"

      "#A{o:o}"))


  (testing "unused pseudos"
    (defcase {:pseudos #{":A"}}
      ":A{o:o}
       :B{o:o}"

      ":A{o:o}"))


  (testing "unused nth-*"
    (defcase {:pseudos #{":nth-child"}}
      ":nth-child(n){o:o}
       :nth-of-type(n){o:o}"

      ":nth-child(n){o:o}"))


  (testing "unused functions"
    (defcase {:functions #{":A"}}
      ":A(n){o:o}
       :B(n){o:o}"

      ":A(n){o:o}"))


  (testing "unused single attribute"
    (defcase {:attributes #{["A"]}}
      "[A]{o:o}
       [B]{o:o}"

      "[A]{o:o}"))


  (testing "unused = attribute"
    (defcase {:attributes #{["A" "W"]}}
      "[A=W]{o:o}
       [A=G]{o:o}
       [B=W]{o:o}"

      "[A=W]{o:o}"))


  (testing "unused ~ attribute"
    (defcase {:attributes #{["A" "C W T"]}}
      "[A~=W]{o:o}
       [A~=G]{o:o}
       [B~=W]{o:o}"

      "[A~=W]{o:o}"))


  (testing "unused ^ attribute"
    (defcase {:attributes #{["A" "WT"]}}
      "[A^=W]{o:o}
       [A^=T]{o:o}"

      "[A^=W]{o:o}"))


  (testing "unused $ attribute"
    (defcase {:attributes #{["A" "WT"]}}
      "[A$=T]{o:o}
       [A$=W]{o:o}"

      "[A$=T]{o:o}"))


  (testing "unused * attribute"
    (defcase {:attributes #{["A" "CWT"]}}
      "[A*=W]{o:o}
       [A*=G]{o:o}"

      "[A*=W]{o:o}"))


  (testing "unused | attribute"
    (defcase {:attributes #{["A" "C-S"]
                            ["H" "C"]}}
      "[A|=C]{o:o}
       [H|=C]{o:o}
       [A|=S]{o:o}
       [H|=S]{o:o}"

      "[A|=C]{o:o}"
      "[H|=C]{o:o}"))


  (testing "not"
    (defcase {:attributes #{["A"]}}
      ":not([B]){o:o}
       :not([A]){o:o}"

      ":not([A]){o:o}"))


  (testing "combinators"
    (defcase {:types #{"A" "B"}}
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
      "A~B{o:o}"))


  (testing "empty declaration"
    (defcase {:types #{"A"}}
      "A{}"

      ""))


  (testing "duplicate declaration"
    (defcase {:types #{"A"}}
      "A{a:1;a:2;b:3}"

      "A{a:2;b:3}"))


  (testing "duplicate selector"
    (defcase {:types #{"A"}}
      "A{a:a}
       A{b:b}"

      "A{b:b;a:a}"))


  (testing "unused variables"
    (defcase {:types #{"A" "B" "C"}}
      "A{--a:1}
       B{--b:2}
       C{c:var(--a);
         d:var(--d)}"

      "A{--a:1}"
      "C{c:var(--a)}")))



(deftest keyframe-rule
  (testing "unused keyframes"
    (defcase {:types #{"A"}}
      "A{animation: B}
       @keyframes B{}
       @keyframes C{}"

      "@keyframes B{}"
      "A{animation:B}")))



(deftest media-rule
  (testing "empty rules"
    (defcase {:types #{"A"}}
      "@media all{A{o:o}}
       @media all{B{o:o}}
       @media all{}
       @media all{@media all{A{o:o}}}
       @media all{@media all{B{o:o}}}"

      "@media all{A{o:o}}"
      "@media all{@media all{A{o:o}}}"))

  (testing "duplicate selectors"
    (defcase {:types #{"A"}}
      "A{o:o}
       @media all{A{o:o}}"

      "@media all{A{o:o}}"
      "A{o:o}")))

