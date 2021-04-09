(ns cleancss.watcher-test
  (:require
   [cleancss.watcher  :as sut]
   [clojure.java.io   :as io]
   [clojure.test      :refer :all]
   [matcho.core       :refer [match]]))


(deftest watcher-test
  (testing "extract-classes"

    (match
     (sut/extract-classes "#c\"foo\"")
     #{"foo"})

    (match
     (sut/extract-classes "#c  \"foo\"")
     #{"foo"})

    (match
     (sut/extract-classes "#c \"foo\" #c \"bar\"")
     #{"foo" "bar"})

    (match
     (sut/extract-classes "#c \"foo\" 
                           #c,\"bar\"")
     #{"foo" "bar"}))


  (testing "user-file-path"
    (match
     (sut/user-file-path
      (io/file "test/cleancss/watcher_test.clj"))
     "test/cleancss/watcher_test.clj"))


  (testing "cache-file-path"
    (match 
     (sut/cache-file-path
      "resources/public/css/out"
      "src/app/core.cljs")
     "resources/public/css/out/src/app/core.cljs.edn"))


  (testing "directory-files"
    (match
     (map #(.getName %) (sut/directory-files "test/cleancss")) 
     ["watcher_test.clj" "clean_test.clj"]))


  (testing "content-selectors"
    (match
     (sut/content-selectors
      "#c \"foo\" #i \"bar\"")
     {:classes     #{"foo"}
      :identifiers #{"bar"}}))


  (testing "add-default-selectors"
    (match
     (sut/add-default-selectors
      {:configuration {:default nil}}
      {:classes #{"foo" "bar"}})
     {:classes #{"foo" "bar"}})

    (match
     (sut/add-default-selectors
      {:configuration {:default {:classes #{"foo"}}}}
      {}) 
     {:classes #{"foo"}})

    (match
     (sut/add-default-selectors
      {:configuration {:default {:classes #{"baz"}}}}
      {:classes #{"foo" "bar"}}) 
     {:classes #{"foo" "bar" "baz"}})))
