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
  "Extract the Base 64 String from the XML path"
  [zip]
  (let [candidates
        (map zip/node
             (map zip/down
                  (zx/xml-> zip :application-desc :argument)))        
        c-wo-eol
        (map #(s/replace % #"\n" "") candidates)]
    (first (filter #(re-matches b64rx %) c-wo-eol))))

(defn process [config]
  "convert jnlps to xidvs"
  (doseq [f  (u/list-files (:in-dir config) (:file-ext-in config))]
    (let [x (-> f io/input-stream xml/parse zip/xml-zip) 
          o (first (s/split (.getName f) #"[.]"))
          b (b64 x)
          s (try
              (String. (.decode (Base64/getDecoder) b) "utf-8")
              (catch Exception e (str (.getMessage e))))
          xidv (str (:out-dir config)
                    (java.io.File/separator) o "."(:file-ext-out config))]
      (spit xidv (u/ppxml s)))))


;; for example

(def my-config
  {:in-dir "/Users/chastang/Desktop/jnlps/"
   :out-dir "/tmp/xidvs/"
   :file-ext-in "jnlp"
   :file-ext-out "xidv"})

(io/make-parents (str (:out-dir my-config) "foo")) ;;kludge

(process my-config)
