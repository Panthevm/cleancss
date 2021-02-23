(ns ^:figwheel-hooks app.core
  (:require
   [reagent.dom    :as dom]
   [cleancss.cljs.state :refer [c]]
   [app.components.sidebar.view :as sidebar]

   [app.pages.intro.view :as intro]))

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
