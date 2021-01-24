 (ns ^:figwheel-hooks app.core
   (:require
    [reagent.dom    :as dom]
    [cleancss.utils :refer [c]]))


(defn view
  []
  [:section {:class (c "min-h-screen" "flex" "items-center" "justify-around" "bg-red-500")}
   [:div {:class (c "h-32" "w-32" "relative" "cursor-pointer")}
    [:div {:class (c "absolute" "inset-0" "bg-white" "opacity-25" "rounded-lg" "shadow-2xl")}]
    [:div {:class (c "absolute" "inset-0" "transform" "hover:scale-75" "transition")}
     [:div {:class (c "h-full" "w-full" "bg-white" "rounded-lg" "shadow-2xl")}]]]])

(defn ^:after-load mount
  []
  (dom/render [view] (js/document.getElementById "app")))

(mount)
