(ns xidv-tool.xidv2
  (:require
   [xidv-tool.util :as u]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [clojure.zip :as zip]
   [clojure.data.zip.xml :as zx]
   [clojure.data.xml :as xml]))

(def f "/Users/chastang/Desktop/jnlps/volume.xidv")

(def x (-> f io/input-stream xml/parse))

(def xz (-> x zip/xml-zip))

;; http://ravi.pckl.me/short/functional-xml-editing-using-zippers-in-clojure/
(defn tree-edit
  "Take a zipper, a function that matches a pattern in the tree,
   and a function that edits the current location in the tree.  Examine the tree
   nodes in depth-first order, determine whether the matcher matches, and if so
   apply the editor."
  [zipper matcher editor]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (if-let [matcher-result (matcher loc)]
        (let [new-loc (zip/edit loc editor)]
          (if (not (= (zip/node new-loc) (zip/node loc)))
            (recur (zip/next new-loc))))
        (recur (zip/next loc))))))

(defn match? [loc]
  (let [n (zip/node loc)]
    (and (string? n) (.contains n "upc"))))

;; edit function
(defn editor [node]
  (let [filename (last (s/split node #"/"))]
    (str "/Users/chastang/Desktop/data/" filename)))

(def edited (tree-edit xz match? editor))

(with-open [f (io/writer  "/tmp/volume.xidv")]
  (xml/emit edited f))


