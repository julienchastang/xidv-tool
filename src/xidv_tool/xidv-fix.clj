(ns xidv-tool.xidv-fix
  (:require
   [xidv-tool.util :as u]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [clojure.zip :as zip]
   [clojure.data.zip.xml :as zx]
   [clojure.data.xml :as xml]))

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


(def fs (re-pattern java.io.File/separator))

(defn match-fn
  "Generate a match function based on string"
  [s]
  (fn [loc]
    (let [n (zip/node loc)]
      (and (string? n) (.contains n s)))))

(def match-datadir? (match-fn "upc"))

(def match-motherlode? (match-fn "://motherlode.ucar.edu"))

;; edit function
(defn datadir-editor
  "Edit out currrent data and replace with /tmp/data/ dir"
  [node]
  (let [filename (last (s/split node fs))]
    (str "/tmp/data/" filename)))

(defn motherlode-editor
  "Replace references to motherlode"
  [node]
  (let [parts (s/split node #"/")
        domain (nth parts 2)]
    (.replace node domain "thredds.ucar.edu")))

(defn process
  "fix xidvs"
  [files dest]
  (doseq [f files]
    (let [s (-> f io/input-stream
                xml/parse
                zip/xml-zip
                (tree-edit match-motherlode? motherlode-editor)
                zip/xml-zip
                (tree-edit match-datadir? datadir-editor)              
                (xml/emit-str) (u/ppxml))
          o (str dest
                 (-> f (.getAbsolutePath) (s/split fs) last s/trim))]
      (spit o s))))

;; for example

(def my-config
  {:in-dir "/tmp/xidvs/"
   :file-ext-in "xidv"
   :out-dir "/tmp/xidvs-clean/"})

(io/make-parents (str (:out-dir my-config) "foo")) ;;kludge

(process (u/list-files (:in-dir my-config) (:file-ext-in my-config))
         (:out-dir my-config))
