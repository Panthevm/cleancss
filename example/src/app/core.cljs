(ns app.core
  (:require
   [reagent.dom :as dom]))

(defn view
  []
  [:div {:class [#c"min-h-screen"
                 #c"bg-gray-100"]}
   "Hello World"])


(defn mount
  []
  (dom/render [view] (js/document.getElementById "root")))

(mount)
