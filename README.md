# CleanCSS
CleanCSS - is a tool that removes unused CSS stylesheets

## Usage

1) Add a dependency:

```edn
cleancss/cleancss {:mvn/version "RELEASE"}
```

2) Create the `cleancss.edn` configuration file in the root directory of the project:

```edn
{:application
 {;; :all - Includes all functions `cleancss.defaults/functions`. Or a custom set #{":lang" ...}
  :functions :all

  ;; :all - Includes all pseudos `cleancss.defaults/pseudos`. Or a custom set #{":hover" ...}
  :pseudos :all

  ;; :all - Includes all types `cleancss.defaults/types`. Or a custom set #{"*", "div" ...}
  :types :all

  ;; If you do not specify a value, the data will be taken from the application
  ;; :identifiers #{"#id"}
  ;; :classes     #{".name"}
  ;; :attributes  #{["hidden"] ["hreflang" "en"]}
  }

 :build
 {;; Path to the source css file
  :import {:input-files ["resources/public/css/tailwind.min.css"]}

  ;; The path to the file where the final result will be stored
  :export {:output-file "resources/public/css/cleancss.css"}}}

```

3) Add a call to `cleancss.hooks/reset` before building the project and `cleancss.hooks/build` after building.For example, in [figwheel-main](https://github.com/bhauman/figwheel-main), this is configured as follows:

```edn
  :pre-build-hooks  [cleancss.hooks/reset]
  :post-build-hooks [cleancss.hooks/build]
```

4) Wrap all the styles you use in a macro

```clojure
(ns app.core
  (:require
   [cleancss.utils :as cu]))

(defn component
  []
  ;; classes
  [:nav {:class (cu/classes ["shadow-md" "rounded-lg"])}

   ;; identifiers
   [:button {:id (cu/identifier "id")}
    "Login"]

   ;; attributes, classes, identifiers
   [:button (cu/attribute {:id "send" :class ["save"] :type "button"})
    "Info"]])
```

## Demo
![demo](https://s2.gifyu.com/images/ezgif.com-video-to-gif94a811ffec512930.gif)
