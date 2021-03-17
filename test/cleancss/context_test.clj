(ns cleancss.context-test
  (:require
   [cleancss.context :as    sut]
   [cleancss.data    :as    data]
   [matcho.core      :refer [match]]
   [clojure.test     :refer [deftest testing]]))


(defmacro defcase
  [css context]
  `(match (->> ~css data/string->schema sut/get-context)
          ~context))


(deftest get-context


  (testing "animation"
    (defcase
      "E{animation: name infinite ease-in-out}"
      {:animations #{"name"}})
    (defcase
      "E{animation-name: name}"
      {:animations #{"name"}})))
