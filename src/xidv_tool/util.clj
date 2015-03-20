(ns xidv-tool.util
  (:import
   [java.io ByteArrayInputStream]
   [java.nio.charset StandardCharsets]
   [java.io StringWriter]
   [javax.xml.transform TransformerFactory OutputKeys]
   [javax.xml.transform.stream StreamSource StreamResult])
  (:require
   [clojure.java.io :as io]))

;;http://nakkaya.com/2010/03/27/pretty-printing-xml-with-clojure/
(defn ppxml
  "Pretty print XML strings"
  [xml]
  (with-open [is (-> xml (.getBytes StandardCharsets/UTF_8)
                     (ByteArrayInputStream.))]
    (let [in (StreamSource. is)
          writer (StringWriter.)
          out (StreamResult. writer)
          transformer (.newTransformer 
                       (TransformerFactory/newInstance))]
      (.setOutputProperty transformer 
                          OutputKeys/INDENT "yes")
      (.setOutputProperty transformer 
                          "{http://xml.apache.org/xslt}indent-amount" "2")
      (.setOutputProperty transformer 
                          OutputKeys/METHOD "xml")
      (.transform transformer in out)
      (-> out .getWriter .toString))))

(defn list-files
  "List files in dir with extension"
  [dir ext]
  (filter #(.endsWith (str %) ext)
          (filter #(not (.isDirectory %))
                  (file-seq (io/file dir)))))
