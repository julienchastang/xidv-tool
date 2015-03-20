(ns xidv-tool.jnlp-to-xidv
  (:import
   [java.util Base64])
  (:require
   [clojure.java.io :as io]
   [clojure.xml :as xml]
   [clojure.string :as s]
   [xidv-tool.util :as u]))

;; http://stackoverflow.com/questions/475074/regex-to-parse-or-validate-base64-data
(def b64rx #"^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")

(defn b64
  "Extract the Base 64 String from the JNLP file"
  [data]
  (let [candidates
        (map first
             (map :content
                  (filter #(= (:tag %) :argument)
                          (:content (first
                                     (filter #(= (:tag %) :application-desc)
                                             (:content data)))))))
        c-wo-eol (map #(s/replace % #"\n" "") candidates)]
    (first (filter #(re-matches b64rx %) c-wo-eol))))

(doseq [f  (u/list-files input-directory "jnlp")]
  (let [x (xml/parse (io/input-stream f))
        o (first (s/split (.getName f) #"[.]"))
        b (b64 x)
        s (try
            (String. (.decode (Base64/getDecoder) b) "utf-8")
            (catch Exception e (str (.getMessage e))))
        xidv (str input-directory (java.io.File/separator) o ".xidv" )]
    (spit xidv (u/ppxml s))))
