(ns kioo.common
    (:require [kioo.util :refer [convert-attrs]]))

(defn content [& body]
  (fn [node]
    (assoc node :content body)))

(defn append [& body]
  (fn [node]
    (assoc node :content (concat (:content node) body))))

(defn prepend [& body]
  (fn [node]
    (assoc node :content (concat body (:content node)))))


(defn substitute [& body]
  (fn [node] body))


(defn set-attr [& body]
  (let [els (partition 2 body)]
    (fn [node]
      (assoc node :attrs (reduce (fn [n [k v]]
                                  (assoc n k v))
                                (:attrs node) els)))))

(defn remove-attr [& body]
  (fn [node]
    (assoc node :attrs (reduce (fn [n k]
                                (dissoc n k))
                              (:attrs node) body))))

(defn do-> [& body]
  (fn [node]
    (reduce #(%2 %1) node body)))


(defn set-style [& body]
  (let [els (partition 2 body)
        mp (reduce (fn [m [k v]] (assoc m k v)) {} els)]
    (fn [node]
      (update-in node [:attrs :style] #(merge %1 mp)))))


(defn remove-style [& body]
  (apply set-style  (interleave body (repeat nil))))


(defn- get-class-regex [cls]
  #+cljs (js/RegExp. (str "(\\s|^)" cls "(\\s|$)"))
  #+clj (re-pattern (str "(\\s|^)" cls "(\\s|$)")))

(defn- has-class? [cur-cls cls]
  (.match cur-cls (get-class-regex cls)))


(defn set-class [& values]
  (fn [node]
    (let [new-class (reduce #(if (has-class? %1 %2)
                               %1
                               (str %1 " " %2))
                            ""
                            values)]
      (assoc-in node [:attrs :className] new-class))))


(defn add-class [& values]
  (fn [node]
    (let [new-class (reduce #(if (has-class? %1 %2)
                               %1
                               (str %1 " " %2))
                        (get-in node [:attrs :className])
                        values)]
      (assoc-in node [:attrs :className] new-class))))


(defn remove-class [& values]
  (fn [node]
    (let [new-class (reduce #(if (has-class? %1 %2)
                               (.replace %1 (get-class-regex %2) " ")
                               %1)
                        (get-in node [:attrs :className])
                        values)]
      (assoc-in node [:attrs :className] new-class))))

(defn wrap [tag attrs]
  (fn [node]
    {:tag tag
     #+cljs :sym #+cljs (aget js/React.DOM (name tag)) 
     :attrs (convert-attrs attrs)
     :content [node]}))

(defn unwrap [node]
  (:content node))