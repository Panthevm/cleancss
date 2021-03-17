(ns cleancss.context)


(defn add-animation
  [context declaration]
  (update context :animations
          conj (re-find #"\S+" (:expression declaration))))


(defn get-context
  [stylesheets]
  (loop [nodes   (seq stylesheets)
         context {:used-variables #{}
                  :animations     #{}
                  :variables      #{}}]
    (if nodes
      (let [node      (first nodes)
            node-type (:type node)]
        (cond
          (= :declaration node-type)
          (cond 

            (#{"animation" "animation-name"} (:property node))
            (recur (next nodes) (add-animation context node))

            :else (recur (next nodes) context))

          (= :style-rule node-type)
          (recur (into (next nodes) (:declarations node)) context)

          (= :media-rule node-type)
          (recur (into (next nodes) (:rules node)) context)

          :else (recur (next nodes) context)))

      context)))
