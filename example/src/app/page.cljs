(ns app.page
  (:require
   [cleancss.state :refer [c]]))

(defn view
  []
  [:section {:class (c "min-h-screen" "bg-red-500")}])
