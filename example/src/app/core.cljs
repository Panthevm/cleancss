 (ns ^:figwheel-hooks app.core
  (:require
   [reagent.dom :as dom])
  (:require-macros
   [cleancss.utils :refer (classes)]))


;; 2.21
(defn view
  []
  [:section {:class (classes ["min-h-screen" "flex" "items-center" "justify-around" "bg-red-500"])}
   [:div {:class (classes ["h-32" "w-32" "relative" "cursor-pointer"])}
    [:div {:class (classes ["absolute" "inset-0" "bg-white" "opacity-25" "rounded-lg" "shadow-2xl"])}]
    [:div {:class (classes ["absolute" "inset-0" "transform" "hover:scale-75" "transition"])}
     [:div {:class (classes ["h-full" "w-full" "bg-white" "rounded-lg" "shadow-2xl"])}]]]])

(defn ^:after-load mount
  [& args]
  (dom/render [view] (js/document.getElementById "app")))

(mount)
