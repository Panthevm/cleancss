(ns cleancss.compression-test
  (:require
   [cleancss.compression :as    sut]
   [cleancss.data        :as    data]
   [clojure.test         :refer [deftest testing]]
   [matcho.core          :refer [match]]))


(defmacro defcase
  [before & after]
  `(match
    (->> (data/string->schema ~before)
         (sut/make)
         (data/schema->string))
    (apply str ~@after)))


(deftest make


  (testing "duplicate declarations"
    (defcase
      "E{a:1;b:2;a:3}"

      "E{a:3;b:2}"))


  (testing "duplicate selectors"
    (defcase
      "A,A,B{a:a}
       A A,A A{a:a}"

      "A,B{a:a}"
      "A A{a:a}"))


  (testing "duplicate style"
    (defcase
      "E{a:1}
       E{a:2}"

      "E{a:2}"))


  (testing "duplicate media"
    (defcase
      "@media all {A{}}
       @media all {B{}}
       A{}"

      "@media all{A{}B{}}"
      "A{}"))


  (testing "duplicate keyframes"
    (defcase
      "@keyframes A {from {color: red}}
       @keyframes A {from {right: 100}}
       @keyframes A {to   {right: 100}}"

      "@keyframes A{from{color:red;right:100}to{right:100}}")))
