<p align="center"><a href="https://github.com/Panthevm/cleancss"><img src="https://i.ibb.co/HrFyQQ8/cleancss.png" alt="logo"></a></p>

# CleanCSS

[![Clojars Project](https://img.shields.io/clojars/v/cleancss.svg)](https://clojars.org/cleancss)

CleanCSS - ClojureScript tool that removes unused CSS

## Usage

1) Add a dependency:


```edn
cleancss/cleancss {:mvn/version "0.6.0"}
```

2) Create the `cleancss.edn` configuration file in the root directory of the project:

```edn
{;; project sources
 :watch-dirs ["src"]
 
 :state
 {;; If not specified, the value will be `cleancss.cljs.env/functions`
  ;; :functions #{":lang" ...}

  ;; If not specified, the value will be `cleancss.cljs.env/pseudos`
  ;; :pseudos #{":hover" ...}

  ;; If not specified, the value will be `cleancss.cljs.env/types`
  ;; :types #{"*", "div" ...}

  ;; If you do not specify a value, the data will be taken from the application
  ;; :identifiers #{"id"}
  ;; :classes     #{"name"}
  ;; :attributes  #{["hidden"] ["hreflang" "en"]}
  }

 :build
 {;; Path to the source css file
  :import {:input-files ["resources/public/css/tailwind.min.css"]}

  ;; The path to the file where the final result will be stored
  :export {:output-file "resources/public/css/cleancss.css"}}}

```

3) Add a call to `cleancss.cljs.hooks/reset` before building the project and `cleancss.cljs.hooks/build` after building.For example, in [figwheel-main](https://github.com/bhauman/figwheel-main), this is configured as follows:

```edn
  :css-dirs         ["resources/public/css"]
  :pre-build-hooks  [cleancss.cljs.hooks/reset]
  :post-build-hooks [cleancss.cljs.hooks/build]
```

4) Wrap all the styles you use in a macro

```clojure
(ns app.core
  (:require
   [cleancss.cljs.state :refer [c i a]))

(defn component
  []
  ;; classes
  [:nav {:class (c "shadow-md" "rounded-lg")}

   ;; identifiers
   [:button {:id (i "id")}
    "Login"]

   ;; attributes, classes, identifiers
   [:button (a {:id "send" :class ["save"] :type "button"})
    "Info"]])
```

## Workflow

![workflow](https://imageshost.ru/images/2021/03/28/Untitled-Diagram2.png)

## Demo
![demo](https://s2.gifyu.com/images/simplescreenrecorder-2021-01-26.gif)

