(ns cleancss.state
  #?(:clj
     (:require
      [clojure.string :as string]))
  #?(:cljs
     (:require-macros
      [cleancss.state])))


#?(:clj (defonce state (atom {})))


#?(:clj
   (defn get-classes
     [app-state]
     (apply merge
            (map (comp :classes second)
                 app-state))))


#?(:clj
   (defn get-identifiers
     [app-state]
     (apply into
            (map (comp :identifiers second)
                 app-state))))


#?(:clj
   (defn get-attributes
     [app-state]
     (apply into
            (map (comp :attributes second)
                 app-state))))


#?(:clj
   (defn escape
     [^String value]
     (string/replace (name value) #":" "\\\\:")))


#?(:clj
   (defn add-identifier
     [state name-space identifier]
     (update-in state [name-space :identifiers]
                (fnil conj #{}) identifier)))


#?(:clj
   (defn add-attributes
     [state name-space attributes]
     (update-in state [name-space :attributes]
             (fnil into #{})
             (keep (fn [[attribute-name attribute-value]]
                     (when-not (fn? attribute-value)
                       (if (boolean? attribute-value)
                         [(name attribute-name)]
                         [(name attribute-name) attribute-value])))
                   attributes))))


#?(:clj
   (defn alias-classes
     [state classes]
     (vec (vals (select-keys (get-classes state) classes)))))


#?(:clj
   (defn add-classes
     [state name-space classes]
     (update-in state [name-space :classes]
                (fn [ns-classes]
                  (loop [items  (seq classes)
                         index  0
                         result (or ns-classes {})]
                    (if items
                      (let [item        (first items)
                            app-classes (get-classes state)]
                        (if (contains? app-classes item)
                          (recur (next items)
                                 index
                                 (assoc result (escape item)
                                        (get app-classes item)))
                          (recur (next items)
                                 (inc  index)
                                 (assoc result (escape item)
                                        (cleancss.compression/short-name
                                         (+ index (count app-classes)))))))
                      result))))))


#?(:clj
   (defmacro c [& classes]
     (let [name-space (-> &env :ns :name)]
       (swap! state #(add-classes % name-space classes))
       (alias-classes @state classes))))


#?(:clj
   (defmacro i [identifier]
     (let [name-space (-> &env :ns :name)]
       (swap! state #(add-identifier % name-space identifier))
       identifier)))


#?(:clj
   (defmacro a [attributes]
     (let [name-space (-> &env :ns :name)]

       (when (:id attributes)
         (swap! state #(add-identifier % name-space (:id attributes))))

       (when (seq (:class attributes))
         (swap! state #(add-classes % name-space (:class attributes))))

       (when (seq attributes)
         (swap! state #(add-attributes % name-space attributes)))

       attributes)))


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
    ":not"
    ":dir"
    ":has"
    ":host"
    ":lang"
    ":where"
    ":host-context"})
