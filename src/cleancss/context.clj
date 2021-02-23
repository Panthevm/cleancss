(ns cleancss.context)


(defn add-animation
  [context declaration]
  (update context :animations
          conj (-> declaration :meta :animation)))


(defn add-variable
  [context declaration]
  (update context :variables
          conj (:property declaration)))


(defn add-used-variables
  [context declaration]
  (update context :used-variables
          into (->> declaration :meta :variables
                    (map (fn [variable]
                           (first (re-seq #"[^,]+" variable)))))))


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
          (let [declaration-type (-> node :meta :type)]
            (cond 

              (= :animation declaration-type)
              (recur (next nodes) (add-animation context node))

              (= :variable declaration-type)
              (recur (next nodes) (add-variable context node))

              (-> node :meta :variables)
              (recur (next nodes) (add-used-variables context node))

              :else (recur (next nodes) context)))

          (= :style-rule node-type)
          (recur (into (next nodes) (:declarations node)) context)

          (= :media-rule node-type)
          (recur (into (next nodes) (:rules node)) context)

          :else (recur (next nodes) context)))

      context)))
