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
    (defcase {:classes {"wery-long-name" "a"}}
      ".wery-long-name{o:o}
       .B{o:o}"

      ".a{o:o}"))


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

      "")))


(deftest media-rule

  (testing "empty rules"

    (defcase {:types #{"A"}}
      "@media all{A{o:o}}
       @media all{B{o:o}}
       @media all{}
       @media all{@media all{A{o:o}}}
       @media all{@media all{B{o:o}}}"

      "@media all{A{o:o}}"
      "@media all{@media all{A{o:o}}}")))


(deftest remove-by-context


  (testing "duplicate selector"
    (defcase {:types #{"A"}}
      "A{a:a}
       A{b:b}"

      "A{b:b;a:a}")

    (defcase {:types #{"A"}}
      "A{o:o}
       @media all{A{o:o}}"

      "@media all{A{o:o}}"
      "A{o:o}"))


  (testing "duplicate declaration"
    (defcase {:types #{"A"}}
      "A{a:1;a:2;b:3}"

      "A{a:2;b:3}"))


  (testing "unused keyframes"
    (defcase {:types #{"A"}}
      "A{animation: B}
       @keyframes B{}
       @keyframes C{}"

      "@keyframes B{}"
      "A{animation:B}"))


  (testing "variables short alias"
    (defcase {:types #{"A" "B"}}
      "A{--first-name:1; --second-name:1}
       B{b:calc(var(--first-name) - var(--second-name))}"

      "A{--b:1;--a:1}"
      "B{b:calc(var(--b) - var(--a))}"))


  (testing "unused variables"
    (defcase {:types #{"A" "B" "C"}}
      "A{--a:1}
       B{--b:2}
       C{c:var(--a);
         d:var(--d)}"

      "A{--a:1}"
      "C{c:var(--a)}"))


  (testing "variables defaults resolve"
    (defcase {:types #{"A" "B" "C"}}
      "A{--a:1}
       B{b:calc(1 - var(--a, 0))}
       C{c:calc(1 - var(--c, 0))}"

      "A{--a:1}"
      "B{b:calc(1 - var(--a))}"
      "C{c:calc(1 - 0)}")))
