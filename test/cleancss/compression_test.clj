(ns cleancss.compression-test
  (:require
   [cleancss.compression :as    sut]
   [cleancss.import      :as    import]
   [cleancss.export      :as    export]
   [clojure.test         :refer [deftest testing]]
   [matcho.core          :refer [match]]))


(defmacro defcase
  [app before & after]
  `(match
    (->> (import/from-string ~before)
         (sut/make (:state ~app) (:context ~app))
         (export/to-string))
    (apply str ~@after)))


(deftest make


  (testing "short name variable"
    (defcase {:context {:variables {"--very-long-name" "--a"}}}
      "A{--very-long-name:1}
       B{b:var(--very-long-name)}"
      "A{--a:1}"
      "B{b:var(--a)}"))


   (testing "variables resolve default"
    (defcase {:context {:variables {"--name" "--a"}}}
      "E{a:var(--name, 1);
         b:var(--empty, 2)}"
      "E{a:var(--a);b:2}"))


  (testing "short name classes"
    (defcase {:state {:classes {"very-long-name" "a"}}}
      ".very-long-name{a:1}"

      ".a{a:1}"))


  (testing "duplicate declarations"
    (defcase {}
      "E{a:1;b:2;a:3}"

      "E{a:3;b:2}"))


  (testing "duplicate selectors"
    (defcase {}
      "A,A,B{a:a}
       A A,A A{a:a}"

      "A,B{a:a}"
      "A A{a:a}"))


  (testing "duplicate style"
    (defcase {}
      "E{a:1}
       E{a:2}"

      "E{a:2}"))


  (testing "duplicate media"
    (defcase {}
      "@media all {A{}}
       @media all {B{}}
       A{}"

      "@media all{A{}B{}}"
      "A{}"))


  (testing "duplicate keyframes"
    (defcase {}
      "@keyframes A {from {color: red}}
       @keyframes A {from {right: 100}}
       @keyframes A {to   {right: 100}}"

      "@keyframes A{from{color:red;right:100}to{right:100}}")))
