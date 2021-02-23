(ns app.components.sidebar.view
  (:require
   [app.components.sidebar.model :as model]
   [cleancss.cljs.state          :refer [c]]))

(defn component
  []
  [:div {:class (c "p-8"
                   "flex"
                   "w-full"
                   "relative"
                   "max-w-xs"
                   "bg-white"
                   "shadow-md"
                   "rounded-r-lg")}
   [:ul {:class (c "w-full")}
    [:li {:class (c "my-px")}
     [:span {:class (c "flex"
                       "my-3"
                       "text-lg"
                       "font-medium"
                       "text-gray-400")}
      "CleanCSS"]]
    (for [link model/sections]
      [:li {:class (c "my-px")}
       [:a {:class (c "flex"
                      "h-12"
                      "rounded-lg"
                      "items-center"
                      "text-gray-600"
                      "focus:shadow-inner"
                      "hover:bg-gray-100")
            :href "#/"}
        [:span {:class (c "ml-3")}
         (:title link)]]])]])
