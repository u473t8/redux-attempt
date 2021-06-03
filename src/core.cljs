(ns core
  (:require [cljs.core.async :as a]
            [reagent.dom :as rdom]
            [reagent.core :as r]
            [clojure.string :as str]))

(def enter-key 13)

(defonce !storage (r/atom []))

(defn redo
  [{:keys [storage] :as state}]
  (update state :storage
          #(reduce
            (fn [storage {:keys [type] :as action}]
              (let [{last-type :type} (last storage)]
                (cond
                  (and (= last-type :undo) (= type :redo)) (pop storage)
                  :else (conj storage action)))) [] %)))

(defn undo
  [{:keys [storage] :as state}]
  (update state :storage
          #(reduce
            (fn [storage {:keys [type] :as action}]
              (cond
                (and (seq storage) (= type :undo)) (pop storage)
                :else (conj storage action))) [] %)))

(defn add-items
  [{:keys [storage] :as state}]
  (reduce
   (fn [{{:keys [idx]} :todo  :as state} {:keys [type entry] :as action}]
     (let [idx (inc idx)]
       (cond-> state
         (= type :add-item)
         (->
          (assoc-in [:todo :idx] idx)
          (update-in [:todo :list] conj {:idx idx :entry entry})))))
   state storage))

(defn delete-items
  [{:keys [storage] :as state}]
  (reduce
   (fn [state {:keys [type idx] :as action}]
     (cond-> state
       (= type :delete-item)
       (update-in [:todo :list]
                  (fn [list]
                    (remove #(= (:idx %) idx) list)))))
   state storage))

(defn todo-list
  [storage]
  (-> {:storage storage
       :todo {:idx 0
              :list []}}
      redo
      undo
      add-items
      delete-items
      (get-in [:todo :list])))

(defn dispatch! [action]
  (r/rswap! !storage conj action))

(defn on-key-down [event]
  (let [key-pressed (.-which event)
        entry (.. event -target -value)]
    (when (and (= key-pressed enter-key)
               (not (str/blank? entry)))
      (dispatch! {:type :add-item :entry entry}))))

(defn todo-input
  []
  [:input.input
   {:style {:margin-top "50px"
            :margin-bottom "25px"}
    :placeholder "What needs to be done?"
    :on-key-down on-key-down}])

(defn undo-button
  []
  [:button.button
   {:style {:margin-top "20px"
            :margin-right "10px"
            :margin-bottom "20px"}
    :on-click #(dispatch! {:type :undo})}
   "Undo"])

(defn redo-button
  []
  [:button.button
   {:style {:margin-top "20px"
            :margin-bottom "20px"}
    :on-click #(dispatch! {:type :redo})}
   "Redo"])

(defn todo-item
  [{:keys [id entry on-delete] :as props}]
  [:div.is-flex.is-justify-content-space-between.is-align-items-baseline.todo-item
   {:id id}
   [:div
    [:span
     {:style {:margin "0 5px"
              :color "gray"}} (str id ".")]
    [:span entry]]
   [:div
    {:on-click #(dispatch! {:type :delete-item :idx id})}
    [:div.delete-item "🗑"]]])

(defn main-page []
  (let [todo-list @(r/track! todo-list @!storage)]
    [:div.is-flex.is-justify-content-center
     [:div.app
      [todo-input]
      [:div
       [undo-button]
       [redo-button]]
      (for [{:keys [idx entry]} todo-list]
        ^{:key idx}
        [todo-item {:id idx :entry entry}])]]))

(rdom/render
 [main-page]
 (.getElementById js/document "app"))
