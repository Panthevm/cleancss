(ns cleancss.compression
  (:require
   [cleancss.find    :as find]
   [clojure.string   :as string]))

(def symbols        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
(def symbols-length (count symbols))

(defn short-name
  [number]
  (loop [iterator number
         result   nil]
    (let [remainder (mod iterator symbols-length)]
      (cond
        (and (zero? iterator) (not result))
        (str (nth symbols remainder))

        (zero? iterator)
        result

        :else
        (recur (/ (- iterator remainder) symbols-length)
               (str result (nth symbols remainder)))))))


(defn variables-resolve
  [context declaration]
  (reduce
   (fn [expression variable]
     (let [[value default] (string/split variable #",")]
       (if (find/variable? context value)
         (string/replace expression (re-pattern variable) (get-in context [:variables value]))
         (string/replace expression (re-pattern (str "var\\(" variable "\\)")) default))))
   (-> declaration :expression)
   (-> declaration :meta :variables)))


(defn declaration
  [context node]
  (cond
    (= :variable (-> node :meta :type))
    (assoc node :property (get-in context [:variables (:property node)]))

    (-> node :meta :variables)
    (assoc node :expression (variables-resolve context node))
    
    :else node))


(defn selector
  [state node]
  (update node :members
          (partial map
                   (fn [member]
                     (if (= :class (:group member))
                       (assoc member :value (str "." (get-in state [:classes (:name member)])))
                       member)))))


(defn make
  [state context stylesheets]
  (letfn [(add-unique [uniques identifier path node]
            (update uniques identifier
                    (fn [unique]
                      (if unique
                        (update unique path #(make state context (into (get node path) %)))
                        (update node   path (partial make state context))))))]

    (loop [nodes   (seq stylesheets)
           uniques {}]
      (if nodes
        (let [node      (first nodes)
              node-type (:type node)]
          (cond

            (= :declaration node-type)
            (let [identifier (:property node)]
              (recur (next nodes) (assoc uniques identifier (declaration context node))))

            (= :selector node-type)
            (let [identifier (hash (:members node))]
              (recur (next nodes) (assoc uniques identifier (selector state node))))

            (= :style-rule node-type)
            (let [new-node   (update node :selectors (partial make state context))
                  identifier (hash (:selectors new-node))]
              (recur (next nodes) (add-unique uniques identifier :declarations new-node)))

            (= :media-rule node-type)
            (let [identifier (hash (:queries node))]
              (recur (next nodes) (add-unique uniques identifier :rules node)))

            (= :keyframes-block node-type)
            (let [identifier (hash (:selectors node))]
              (recur (next nodes) (add-unique uniques identifier :declarations node)))

            (= :keyframes-rule node-type)
            (let [identifier (hash (:name node))]
              (recur (next nodes) (add-unique uniques identifier :blocks node)))
            


            :else 
            (recur (next nodes) (assoc uniques (hash node) node))))

        (vals uniques)))))
