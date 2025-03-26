(ns clojure-todolist.ui.components
  (:require [seesaw.core :as s]
            [seesaw.font :as font]
            [seesaw.border :as border]
            [clojure-todolist.ui.styles :as styles])
  (:import [java.awt Color]
           [javax.swing JCheckBox]))

(defn styled-button [text color]
  (s/button :text text
            :background (Color/decode color)
            :foreground Color/WHITE
            :font (font/font :name "Arial" :style :bold :size 12)))

(defn create-input-panel []
  (let [title-input (s/text
                     :text "Titre de la tâche"
                     :font (font/font :name "Arial" :size 14)
                     :columns 15)
        desc-input (s/text
                    :text "Description"
                    :font (font/font :name "Arial" :size 14)
                    :columns 20)
        add-button (styled-button "Ajouter" styles/primary-color)]

    {:panel (s/border-panel
             :center (s/horizontal-panel
                      :items [title-input desc-input add-button]
                      :border (border/compound-border
                               (border/empty-border :thickness 15)
                               (border/line-border :color "#e0e0e0" :thickness 1))
                      :background (Color/decode styles/card-bg))
             :background (Color/decode styles/bg-color))
     :title-input title-input
     :desc-input desc-input
     :add-button add-button}))

(defn create-header []
  (let [title-label (s/label
                     :text "Gestionnaire de Tâches"
                     :font (font/font :name "Arial" :style :bold :size 24)
                     :foreground (Color/decode styles/primary-color))]
    (s/horizontal-panel
     :items [title-label]
     :border (border/empty-border :thickness 20)
     :background (Color/decode styles/bg-color))))

(defn create-select-panel []
  (let [select-all-checkbox (JCheckBox. "Tout")]
    {:panel (s/horizontal-panel
             :items [select-all-checkbox]
             :border (border/empty-border :thickness 10)
             :background (Color/decode styles/bg-color))
     :select-all select-all-checkbox}))