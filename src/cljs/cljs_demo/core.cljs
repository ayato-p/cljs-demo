(ns cljs-demo.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [clojure.browser.dom :as dom]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

(def initial-state
  {:articles []
   :alert false})

;;; Helper functions

(defn gen-id [articles]
  (inc (count articles)))

(defn read-article-data []
  (into {}
        (map #(vector (keyword %)
                      (-> % dom/get-element dom/get-value))
             ["title" "content"])))

(defn article-validator [{:as m :keys [title content]}]
  (and (seq title)
       (seq content)))

(defn make-data [id]
  (let [title (dom/get-value (dom/get-element "title"))
        content (dom/get-value (dom/get-element "content"))]
    {:id id :title title :content content}))

;;; Event handlers

(register-handler
 ::initialize
 (fn [db _]
   (merge db initial-state)))

(register-handler
 ::article
 (path [:articles])
 (fn [articles _]
   (do
     (dispatch [::silence])
     (conj articles (make-data (gen-id articles))))))

(register-handler
 ::alert
 (path [:alert])
 (fn [alert _] true))

(register-handler
 ::silence
 (path [:alert])
 (fn [alert _] false))

;;; Subscription handlers

(register-sub
 ::articles
 (fn [db _]
   (reaction (:articles @db))))

(register-sub
 ::alert
 (fn [db _]
   (reaction (:alert @db))))

;;; Component

(defn articles-component []
  (let [articles (subscribe [::articles])
        div-style {:max-width "10em"
                   :overflow :hidden
                   :text-overflow :ellipsis}]
    [:section.card
     [:header.bg-light-green
      "現在のポスト数: " (count @articles)]
     [:table.full-width
      [:thead
       [:th {:style {:width "10%"}}]
       [:th {:style {:width "45%"}} "タイトル"]
       [:th {:style {:width "45%"}}"テキスト"]]
      [:tbody.zebra
       (for [{:as article :keys [id title content]} @articles]
         ^{:key id}
         [:tr
          [:td [:div (inc id)]]
          [:td [:div {:style div-style} title]]
          [:td [:div {:style div-style} content]]])]]]))

(defn form-component []
  [:section.card
   [:header.bg-light-green "タイトル:"]
   [:input#title.full-width {:type :text}]

   [:header.bg-light-green "テキスト:"]
   [:textarea#content.full-width]

   [:button.full-width.bg-blue
    {:on-click #(dispatch [::article])} "ポストする"]])

(defn app []
  (let [error (reagent/atom false)]
    (fn []
      [:div.row
       [:div.col-md-6
        [:section.card
         (when (deref (subscribe [::alert]))
           [:div.alert.alert-danger [:strong "Error: "] "タイトルとテキストを入力してください"])
         [:header.bg-light-green "タイトル:"]
         [:input#title.full-width {:type :text}]

         [:header.bg-light-green "テキスト:"]
         [:textarea#content.full-width]

         [:button.full-width.bg-blue
          {:on-click #(if (article-validator (read-article-data))
                        (dispatch [::article])
                        (dispatch [::alert]))}
          "ポストする"]]]

       [:div.col-md-6
        [articles-component]]])))

(defn main []
  (when-let [elm (dom/get-element "app")]
    (dispatch-sync [::initialize])
    (reagent/render [app] elm)))
