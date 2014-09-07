(ns chessground
  (:require [cljs.core.async :as a]
            [chessground.api :as api]
            [chessground.handler :as handler]
            [chessground.ui :as ui]
            [chessground.data :as data]
            [chessground.common :refer [pp]])
  (:require-macros [cljs.core.async.macros :as am]))

(extend-type js/NodeList ISeqable (-seq [array] (array-seq array 0)))

(defn ^:export main
  "Application entry point; returns the public JavaScript API"
  [element config]
  (let [chan (a/chan)
        app-data (data/make (or (js->clj config {:keywordize-keys true}) {}))
        app-atom (atom app-data)
        render (fn [app]
                 (js/React.renderComponent
                   (ui/board-component (clj->js (merge app {:chan chan})))
                   element))]
    (render app-data)
    (am/go-loop []
                (let [[k msg] (a/<! chan)]
                  (render (swap! app-atom (handler/process k msg)))
                  (recur)))
    ; (am/go-loop []
    ;             (a/<! (a/timeout 1))
    ;             (a/>! chan [:select-square (str (get "abcdefgh" (rand-int 8)) 2)])
    ;             (recur))
    (api/build chan)))
