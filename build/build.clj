(ns build
  (:require [shadow.cljs.devtools.api :as shadow]))

(defn -main []
  (shadow/release :app))
