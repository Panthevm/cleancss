(ns ^:figwheel-hooks app.core
  (:require
   [cleancss.cljs.state :refer [c]]

   [reagent.dom                 :as dom]
   [app.components.sidebar.view :as sidebar]
   [app.pages.intro.view        :as intro]))

(defn view
  []
  [:div {:class (c "min-h-screen"
                   "flex"
                   "bg-gray-100")}
   [sidebar/component]
   [:div {:class (c "mx-24"
                    "w-full"
                    "my-12")}
    [intro/page]]])


(defn ^:after-load mount
  []
  (dom/render [view] (js/document.getElementById "app")))

(mount)
