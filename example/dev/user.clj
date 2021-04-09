(ns user
  (:require
   [cleancss.watcher  :as watcher]
   [figwheel.main.api :as repl]))


(def figwheel-options
  {:id "app"

   :options
   {:main       'app.core
    :output-to  "resources/public/js/app.js"
    :output-dir "resources/public/js/out"}

   :config
   {:watch-dirs ["src"]
    :css-dirs   ["resources/public/css"]
    :mode :serve

    :ring-server-options
    {:port 3449}}})


(def cleancss-options
  {:watch-dirs ["src"]

   :default
   {:types #{"*" "html" "body" "div"}}

   :build
   {:import {:directory "resources/public/css/src"}
    :cache  {:directory "resources/public/css/out"}
    :export {:file      "resources/public/css/clean.css"}}})


(defn -main
  [& {:as args}]
  (repl/start figwheel-options)
  (watcher/create cleancss-options)
  )
