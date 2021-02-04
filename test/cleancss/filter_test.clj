(ns cleancss.filter-test
  (:require
   [cleancss.filter :as    sut]
   [cleancss.core   :as    core]
   [clojure.test    :refer :all]
   [matcho.core     :as    matcho :refer [match]]))

(defn- clean
  [app css]
  (->> css
       (core/import-from-string)
       (sut/make-clean app)
       (core/export-to-string)))

(deftest style-rule
  (testing "Remove unused selectors"
    (testing "type"
      (match
       (clean
        {:types #{"A"}}
        "A{o:o}
         B{o:o}")
       "A{o:o}"))

    (testing "class"
      (match
       (clean
        {:classes #{"A"}}
        ".A{o:o}
         .B{o:o}")
       ".A{o:o}"))

    (testing "identifier"
      (match
       (clean
        {:identifiers #{"A"}}
        "#A{o:o}
         #B{o:o}")
       "#A{o:o}"))

    (testing "pseudo"
      (match
       (clean
        {:pseudos #{":A"}}
        ":A{o:o}
         :B{o:o}")
       ":A{o:o}"))

    (testing "attribute"
      (testing "single"
        (match
         (clean
          {:attributes #{["A"]}}
          "[A]{o:o}
           [B]{o:o}")
         "[A]{o:o}"))

      (testing "="
        (match
         (clean
          {:attributes #{["A" "W"]}}
          "[A=W]{o:o}
           [A=G]{o:o}
           [B=W]{o:o}")
         "[A=W]{o:o}"))

      (testing "~"
        (match
         (clean
          {:attributes #{["A" "C W T"]}}
          "[A~=W]{o:o}
           [A~=G]{o:o}
           [B~=W]{o:o}")
         "[A~=W]{o:o}"))

      (testing "^"
        (match
         (clean
          {:attributes #{["A" "WT"]}}
          "[A^=W]{o:o}
           [A^=T]{o:o}")
         "[A^=W]{o:o}"))

      (testing "$"
        (match
         (clean
          {:attributes #{["A" "WT"]}}
          "[A$=T]{o:o}
           [A$=W]{o:o}")
         "[A$=T]{o:o}"))

      (testing "$"
        (match
         (clean
          {:attributes #{["A" "WT"]}}
          "[A$=T]{o:o}
           [A$=W]{o:o}")
         "[A$=T]{o:o}"))

      (testing "*"
        (match
         (clean
          {:attributes #{["A" "CWT"]}}
          "[A*=W]{o:o}
           [A*=G]{o:o}")
         "[A*=W]{o:o}"))

      (testing "|"
        (match
         (clean
          {:attributes #{["A" "C-S"]
                         ["H" "C"]}}
          "[A|=C]{o:o}
           [H|=C]{o:o}
           [A|=S]{o:o}
           [H|=S]{o:o}")
         (str
          "[A|=C]{o:o}"
          "[H|=C]{o:o}"))))

    (testing "not"
      (match
       (clean
        {:attributes #{["A"]}}
        ":not([B]){o:o}
         :not([A]){o:o}")
       ":not([B]){o:o}"))

    (testing "combinator"
      (match
       (clean
        {:types #{"A" "B"}}
        "A B{o:o}
         A>B{o:o}
         A+B{o:o}
         A~B{o:o}
         A C{o:o}
         A>C{o:o}
         A+C{o:o}
         A~C{o:o}")
       (str
        "A B{o:o}"
        "A>B{o:o}"
        "A+B{o:o}"
        "A~B{o:o}"))))


  (testing "remove empty declarations"
    (match
     (clean
      {:types #{"A"}}
      "A{}")
     ""))

  (testing "Merge duplicate style declaration"
    (match
     (clean
      {:types #{"A"}}
      "A{a:1;a:2;b:3}")
     "A{a:2;b:3}"))


  (testing "Merge disabled stylesheet"
    (match
     (clean
      {:types #{"A"}}
      "A{a:a}
       A{b:b}")
     "A{b:b;a:a}"))


  (testing "Remove unused variables"

    (match
     (clean
      {:types #{"A" "B"}}
      "A{--a:1}")
     "")

    (match
     (clean
      {:types #{"A"}}
      "A{--a:1;b:var(--a)}")
     "A{--a:1;b:var(--a)}")

    (match
     (clean
      {:types #{"A" "B"}}
      "A{--a:1}
       B{b:var(--a)}")
     "A{--a:1}B{b:var(--a)}")))


;; TODO
(deftest media-rule
  (is true)
  )

;; TODO
(deftest keyframe-rule
  (is true)
  )
