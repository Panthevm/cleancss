(ns cleancss.clean
  (:require
   [clojure.string :as string]))


(defn attribute?
  [state member]
  (some
   (fn [[name' value]]
     (when (= name' (:name member))
       (let [attribute (:attribute member)]
         (case (-> member :operator :name)
           "="  (= value (:attribute member))
           "^=" (string/starts-with? value attribute)
           "$=" (string/ends-with?   value attribute)
           "*=" (string/includes?    value attribute)
           "~=" (some #(= attribute %) (string/split value #" "))
           "|=" (or (= value attribute)
                    (string/starts-with? value (str attribute "-")))
           true))))
   (:attributes state)))


(defn member?
  [state member]
  (let [member-type (:type member)]
    (cond

      (= :member-simple member-type)
      (let [member-group (:group member)]
        (cond
          (= :class member-group)
          (contains? (:classes state) (subs (:value member) 1))

          (= :identifier member-group)
          (contains? (:identifiers state) (subs (:value member) 1))

          (and (= :type member-group)
               (:types state))
          (contains? (:types state) (:value member))


          (and (= :pseudo member-group)
               (:pseudos state))
          (contains? (:pseudos state)
                     (first (string/split (:value member) #"\(")))

          :else true))

      (and (= :member-function member-type)
           (:functions state))
      (contains? (:functions state)
                 (apply str (butlast (:name member))))

      (= :selector-attribute member-type)
      (attribute? state member)

      :else true)))


(defn by-state
  [state nodes]
  (filter
   (fn [node]
     (case (:type node)

       :selector
       (every? (partial member? state)
               (:members node))

       :style-rule
       (seq (by-state state (:selectors node)))

       :media-rule
       (seq (by-state state (:rules node)))

       true))
   nodes))


(defn by-context
  [context nodes]
  (filter
   (fn [node]
     (case (:type node)

       :keyframes-rule
       (contains? (:animations context) (:name node))

       true))
   nodes))


(defn add-animation
  [context declaration]
  (update context :animations
          conj (re-find #"\S+" (:expression declaration))))


(defn compression
  [stylesheets]
  (letfn [(add-unique [uniques identifier path node]
            (update uniques identifier
                    (fn [unique]
                      (if unique
                        (update unique path
                                #(compression
                                  (into (get node path) %)))
                        (update node path compression)))))]

    (loop [nodes   (seq stylesheets)
           uniques {}]
      (if nodes
        (let [node      (first nodes)
              node-type (:type node)]
          (cond

            (= :declaration node-type)
            (recur (next nodes)
                   (assoc uniques (:property node) node))

            (= :selector node-type)
            (recur (next nodes)
                   (assoc uniques (hash (:members node)) node))

            (= :style-rule node-type)
            (let [new-node (update node :selectors compression)]
              (recur (next nodes)
                     (add-unique uniques (hash (:selectors new-node))
                                 :declarations new-node)))

            (= :media-rule node-type)
            (recur (next nodes)
                   (add-unique uniques (hash (:queries node))
                               :rules node))

            (= :keyframes-block node-type)
            (recur (next nodes)
                   (add-unique uniques (hash (:selectors node))
                               :declarations node))

            (= :keyframes-rule node-type)
            (recur (next nodes)
                   (add-unique uniques (hash (:name node))
                               :blocks node))

            :else 
            (recur (next nodes)
                   (assoc uniques (hash node) node))))

        (vals uniques)))))


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


(defn clean
  [schema state]
  (let [state-schema (by-state state schema)
        context      (get-context state-schema)]
    (->> state-schema
         (by-context context)
         (compression))))
