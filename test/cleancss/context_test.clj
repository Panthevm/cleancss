(ns cleancss.context-test
  (:require
   [cleancss.context :as    sut]
   [cleancss.core    :as    core]
   [matcho.core      :refer [match]]
   [clojure.test     :refer [deftest testing]]))


(defmacro defcase
  [css context]
  `(match (->> ~css
               (core/import-from-string)
               (sut/get-context))
          ~context))


(deftest get-context


  (testing "animation"
    (defcase
      "E{animation: name infinite ease-in-out}"
      {:animations #{"name"}})
    (defcase
      "E{animation-name: name}"
      {:animations #{"name"}}))


  (testing "variables"
    (defcase
      "E{--a: 0}"
      {:variables #{"--a"}}))


  (testing "used variables"
    (defcase
      "E{a: var(--b)}"
      {:used-variables #{"--b"}})
    (defcase
      "E{a: var(--b, 0)}"
      {:used-variables #{"--b"}})))
