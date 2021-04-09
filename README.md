<p align="center"><a href="https://github.com/Panthevm/cleancss"><img src="https://i.ibb.co/HrFyQQ8/cleancss.png" alt="logo"></a></p>

# CleanCSS

[![Clojars Project](https://img.shields.io/clojars/v/cleancss.svg)](https://clojars.org/cleancss)

CleanCSS - ClojureScript tool that removes unused CSS

## Installation

To install, add the following dependency
```edn
;; Clojure CLI
cleancss/cleancss {:mvn/version "2.0.0"}

;; Leiningen
[cleancss/cleancss "2.0.0"]
```

## Usage

1) Call the `cleancss.watcher/create` at the start of the app.

```clojure
(defonce develop
  (cleancss.watcher/create
   {;; A list of source directories to be watched
    :watch-dirs ["src"]

    :build
    {;; Directory with css source files
     :import {:directory "resources/public/css/src"} 
     ;; The directory where the cached data will be stored
     :cache  {:directory "resources/public/css/out"} 
     ;; Output file
     :export {:file "resources/public/css/clean.css"}}}))


```

2) Mark class names and identifiers with literals

```clojure
[:div {:id #i"id-foo"
       :class [#c"class-bar"
               #c"class-baz"]}
 "Hello"]
```
