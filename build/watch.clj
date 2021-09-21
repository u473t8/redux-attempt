(ns watch
  (:require
    [shadow.cljs.devtools.api :as shadow]
    [shadow.cljs.devtools.server :as shadow.server]))


(defn -main
  {:shadow/requires-server true}
  [& _args]
  (shadow.server/stop!)
  (shadow.server/start!)

  (shadow/watch :app))
