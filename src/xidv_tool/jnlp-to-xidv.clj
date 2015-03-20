(ns xidv-tool.jnlp-to-xidv
  (:import
   [java.util Base64])
  (:require
   [xidv-tool.util :as u]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [clojure.zip :as zip]
   [clojure.data.zip.xml :as zx]
   [clojure.data.xml :as xml]))

;; base 64 regex
;; http://stackoverflow.com/questions/475074/regex-to-parse-or-validate-base64-data
(def b64rx #"^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")

(defn b64
  "Extract the Base 64 String from the JNLP file"
  [zip]
  (let [candidates
        (map zip/node
             (map zip/down
                  (zx/xml-> zip :application-desc :argument)))        
        c-wo-eol
        (map #(s/replace % #"\n" "") candidates)]
    (first (filter #(re-matches b64rx %) c-wo-eol))))

(defn jnlp-to-xidv [dir]
  "convert jnlps to xidvs in the dir directory"
  (doseq [f  (u/list-files dir "jnlp")]
    (let [x (-> f io/input-stream xml/parse zip/xml-zip) 
          o (first (s/split (.getName f) #"[.]"))
          b (b64 x)
          s (try
              (String. (.decode (Base64/getDecoder) b) "utf-8")
              (catch Exception e (str (.getMessage e))))
          xidv (str input-directory (java.io.File/separator) o ".xidv" )]
      (spit xidv (u/ppxml s)))))

;; for example

(def input-directory "/Users/chastang/Desktop/jnlps")

(jnlp-to-xidv input-directory)
