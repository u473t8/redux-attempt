(ns build
  (:require
    [shadow.cljs.devtools.api :as shadow]))


(defn -main
  [& _args]
  (shadow/release :app))
