(ns user
  (:require [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow.server]))

(defn restart-ui []
  (shadow.server/stop!)
  (shadow.server/start!)
  (shadow/watch :app))

(comment
  (restart-ui))
