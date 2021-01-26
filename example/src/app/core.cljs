(ns ^:figwheel-hooks app.core
  (:require
   [reagent.dom    :as dom]
   [app.test       :as test]
   [cleancss.utils :refer [c]]))

(defn view
  []
  [:<>
   [test/view]
   [:section {:class (c "min-h-screen" "bg-red-500")}]])

(defn ^:after-load mount
  []
  (dom/render [view] (js/document.getElementById "app")))


(mount)
