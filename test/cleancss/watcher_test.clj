(ns cleancss.watcher-test
  (:require
   [cleancss.watcher  :as sut]
   [clojure.java.io   :as io]
   [clojure.test      :refer :all]
   [matcho.core       :refer [match]]))


(deftest watcher-test
  (testing "extract-classes"

    (match
     (sut/extract-classes "#c/c\"foo\"")
     #{"foo"})

    (match
     (sut/extract-classes "#c/c  \"foo\"")
     #{"foo"})

    #_(match
     (sut/extract-classes "#c/c \"foo\" #c/c \"bar\"")
     #{"foo" "bar"}))


  (testing "get-user-file-path"
    (match
     (sut/get-user-file-path
      (io/file "test/cleancss/watcher_test.clj"))
     "test/cleancss/watcher_test.clj"))


  (testing "make-cache-file-path"
    (match 
     (sut/make-cache-file-path
      "resources/public/css/out"
      "src/app/core.cljs")
     "resources/public/css/out/src/app/core.cljs.edn") 
    ))

