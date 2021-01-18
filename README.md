# CleanCSS
CleanCSS - is a tool that removes unused CSS stylesheets

## Usage

```clojure
(ns build
  (:require
   [cleancss.core  :as cc]
   [cleancss.utils :as cu]))

(defn -main []
  (->> (cc/import-from-file
        {:input-files ["resources/public/css/tailwind.min.css"]})

       (cc/clean
        {:selectors
         {:identifiers #{"id"}
          :namespaces  #{"*"}
          :attributes  #{["type" "button"]}
          :functions   #{":lang"}
          :types       #{"*" "html" "body" "div"}
          :classes     #{"block"}
          :pseudos     #{"::before" ":root" ":focus" ":hover"}}})

       (cc/export-to-file
        {:output-directory "resources/public/css/cssclean.css"})))

```

## Examples

### Declare

```clojurescript
(ns app.components.navbar.view
  (:require
   [cleancss.utils :as cu]))

(defn view
  [data]
  [:nav {:class (cu/classes ["shadow-md" "rounded-lg"])}
   [:div {:class (cu/classes ["hidden" "md:block" "space-x-8" "p-6" "items-center"])}
    (for [link (-> data :links)]
      [:a (cu/attribute {:id    "id"
                         :class ["font-medium" "text-gray-500" "hover:text-gray-900"]
                         :title (-> link :title)
                         :href  (-> link :href)})
       (:title link)])]])

```

### Build

```clojure
(ns build
  (:require
   [cljs.build.api :as api]
   [cleancss.core  :as cc]
   [cleancss.utils :as cu]))

(def compiler-config
  {:output-to     "build/js/app.js"
   :source-map    "build/js/app.js.map"
   :output-dir    "build/js/out"
   :infer-externs  true
   :parallel-build true
   :optimizations  :advanced
   :cache-analysis true

   :main 'app.core})

(defn -main []
  (api/build "src" compiler-config)
  (->> (cc/import-from-file
        {:input-files ["resources/public/css/tailwind.min.css"]})
       (cc/clean
        {:selectors
         {:classes     @cu/classes-
          :attributes  @cu/attributes-
          :identifiers @cu/identifiers-
          :types       #{"*" "a" "h1" "nav" "div" "html" "body"}
          :pseudos     #{"::before" "::after" "::placeholder" ":root" ":focus" ":hover" ":focus-within"}}})
       (cc/export-to-file
        {:output-directory "resources/public/css/cssclean.css"})))


```
