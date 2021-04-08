(ns cleancss.env
  (:require
   [clojure.edn     :as edn]
   [clojure.java.io :as io]))


(defn build-import-directory
  [context]
  (-> context :build :import :directory))


(defn get-output-file-directory
  [context]
  (-> context :configuration :build :export :file))


(defn get-cache-directory
  [context]
  (-> context :configuration :build :cache :directory))

(defn get-watch-directory
  [context]
  (-> context :configuration :watch-dirs))


(defn get-default-selectors
  [context]
  (-> context :configuration :default))


(def types
  #{"*"
    "a"
    "b"
    "i"
    "p"
    "q"
    "s"
    "u"
    "br"
    "dd"
    "dl"
    "dt"
    "em"
    "h1"
    "h2"
    "h3"
    "h4"
    "h5"
    "h6"
    "hr"
    "li"
    "ol"
    "rp"
    "rt"
    "td"
    "th"
    "tr"
    "ul"
    "bdi"
    "bdo"
    "col"
    "del"
    "dfn"
    "div"
    "img"
    "ins"
    "kbd"
    "map"
    "nav"
    "pre"
    "sub"
    "sup"
    "svg"
    "var"
    "wbr"
    "abbr"
    "area"
    "base"
    "body"
    "cite"
    "code"
    "form"
    "head"
    "html"
    "link"
    "mark"
    "math"
    "menu"
    "meta"
    "ruby"
    "samp"
    "span"
    "time"
    "aside"
    "audio"
    "embed"
    "input"
    "label"
    "meter"
    "param"
    "small"
    "style"
    "table"
    "tbody"
    "tfoot"
    "thead"
    "title"
    "track"
    "video"
    "button"
    "canvas"
    "figure"
    "footer"
    "header"
    "hgroup"
    "iframe"
    "keygen"
    "legend"
    "object"
    "option"
    "output"
    "script"
    "select"
    "source"
    "strong"
    "address"
    "article"
    "caption"
    "command"
    "details"
    "section"
    "summary"
    "colgroup"
    "datalist"
    "fieldset"
    "noscript"
    "optgroup"
    "progress"
    "textarea"
    "blockquote"
    "figcaption"})


(def pseudos
  #{":left"
    ":root"
    ":empty"
    ":first"
    ":focus"
    ":hover"
    ":links"
    ":right"
    ":scope"
    ":valid"
    "::after"
    ":active"
    ":target"
    "::before"
    ":checked"
    ":default"
    ":enabled"
    ":invalid"
    ":visited"
    ":disabled"
    ":in-range"
    ":optional"
    ":required"
    ":read-only"
    ":nth-child"
    ":fullscreen"
    ":last-child"
    ":only-child"
    ":read-write"
    ":first-child"
    ":last-of-type"
    ":only-of-type"
    ":nth-of-type"
    ":out-of-range"
    ":first-of-type"
    ":indeterminate"
    ":nth-last-child"
    ":-moz-focusring"
    ":-moz-ui-invalid"
    ":nth-last-of-type"
    "::-moz-focus-inner"
    "::-webkit-inner-spin-button"
    "::-webkit-outer-spin-button"
    "::-webkit-search-decoration"
    "::-webkit-file-upload-button"})


(def functions
  #{":is"
    ":dir"
    ":has"
    ":host"
    ":lang"
    ":where"
    ":host-context"})
