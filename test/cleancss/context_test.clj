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
      "E{--variable: 0}"
      {:variables {"--variable" "--a"}}))


  (testing "used variables"

    (defcase
      "E{padding: var(--variable)}"
      {:used-variables #{"--variable"}})

    (defcase
      "E{padding: var(--variable, 0)}"
      {:used-variables #{"--variable"}}))


  (testing "data loss"
    (defcase 
      "E{animation-name: name; --variable: 0; padding: var(--variable)}
       @media all {E{animation-name: name2; --variable2: 0; padding: var(--variable2)}
                   @media all {E{animation-name: name3; --variable3: 0; padding: var(--variable3)}}}"
      {:used-variables #{"--variable3" "--variable2" "--variable"}
       :animations     #{"name" "name3" "name2"}
       :variables      {"--variable" "--a"
                        "--variable3" "--b"
                        "--variable2" "--c"}})))
