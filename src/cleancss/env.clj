(ns cleancss.env
  (:require
   [cleancss.data   :as data]
   [clojure.edn     :as edn]
   [clojure.java.io :as io]))


(defonce context
  (atom nil))


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


(defn set-configuration
  [path]
  (-> 
   (edn/read-string (slurp path))
   (update :selectors
           (partial merge
                    {:types     types
                     :pseudos   pseudos
                     :functions functions}))))


(defn set-stylesheets
  [context]
  (->> context
       :configuration :build :import :input-files
       (mapcat data/resource->schema)
       (assoc context :stylesheets)))


(defn initialize-context
  [path]
  (when (.exists (io/file path))
    (swap! context
           (fn [value]
             (-> value
                 (assoc :configuration (set-configuration path))
                 set-stylesheets)))))


(defn get-cache-directory
  [ctx]
  (-> ctx :configuration :output-to))


(defn get-watch-directory
  [ctx]
  (-> ctx :configuration :watch-dirs))


(defn get-output-file-directory
  [ctx]
  (-> ctx :configuration :build :export :output-file))


(defn get-default-selectors
  [ctx]
  (-> ctx :configuration :selectors))


(defn get-stylesheets
  [ctx]
  (-> ctx :stylesheets))
