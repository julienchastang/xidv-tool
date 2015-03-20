(ns xidv-tool.xidv
  (:require
   [xidv-tool.util :as u]
   [clojure.java.io :as io]
   [clojure.zip :as zip]                                                        
   [clojure.xml :as xml]                                                                  
   [clojure.data.zip :as data.zip]                                                        
   [clojure.data.zip.xml :as zf]
   [clojure.string :as s]))

(defn texts [xz]
  (zf/xml-> xz  data.zip/descendants zip/node string?))

(defn grep [s files]
  (for [f files]
    (let 
        [xz (-> f io/input-stream xml/parse zip/xml-zip texts)]
      (vector (str f) (filter #(.contains % s) xz)))))

(def my-files (u/list-files "/Users/chastang/Desktop/jnlps" "xidv"))

(def data-files
  (map (fn [[x y]]
         (vector x (for [f y]
                   (-> f (s/split #":") last s/trim))))
       (grep "upc" my-files)))

(def links (grep "http:" my-files))

(spit "/tmp/foo" (apply str (map #(str % "\n") links)))

(spit "/tmp/foo" (apply str (map #(str % "\n") data-files)))
