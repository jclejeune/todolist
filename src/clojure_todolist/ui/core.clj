(ns clojure-todolist.ui.core
  (:require [seesaw.core :as s]
            [seesaw.font :as font]
            [seesaw.border :as border]
            [clojure-todolist.model :as model])
  (:import [javax.swing UIManager JButton JCheckBox AbstractCellEditor]
           [javax.swing.table DefaultTableModel DefaultTableCellRenderer TableCellRenderer TableCellEditor]
           [java.awt Color]
           [java.awt.event ActionListener]
           [com.formdev.flatlaf FlatLightLaf]))

;; Définition des couleurs et styles
(def primary-color "#4a86e8")
(def success-color "#34a853")
(def danger-color "#ea4335")
(def warning-color "#fbbc05")
(def bg-color "#f9f9f9")
(def card-bg "#ffffff")

;; Déclaration préalable des fonctions
(declare create-edit-dialog)

;; Initialiser le Look and Feel moderne
(defn setup-look-and-feel []
  (try
    (UIManager/setLookAndFeel (FlatLightLaf.))
    (catch Exception e
      (println "Impossible de charger le Look and Feel moderne:" (.getMessage e)))))

;; Modèle de table amélioré avec colonne de sélection
(defn create-table-model []
  (proxy [DefaultTableModel] [(into-array ["Sélection" "ID" "Titre" "Description" "Terminé" "Date" "Actions"])
                              0]
    (isCellEditable [row col]
      (or (= col 0) (= col 6)))))  ;; Seules les colonnes de sélection et d'actions sont éditables

;; Renderer pour les checkboxes dans la table
(defn checkbox-renderer []
  (proxy [TableCellRenderer] []
    (getTableCellRendererComponent [table value isSelected hasFocus row column]
      (let [checkbox (JCheckBox.)]
        (.setSelected checkbox (boolean value))
        (.setBackground checkbox (if isSelected
                                   (Color/decode "#e0e0e0")
                                   (if (even? row)
                                     (Color/decode "#f8f8f8")
                                     (Color/decode "#ffffff"))))
        (.setHorizontalAlignment checkbox JCheckBox/CENTER)
        checkbox))))

;; Classe pour les boutons d'action (utilisée à la fois pour le renderer et l'editor)
(defn create-action-panel [table todos-table row]
  (let [edit-btn (JButton. "Modifier")
        complete-btn (JButton. "Terminé")
        delete-btn (JButton. "Supprimer")
        current-id (-> table (.getValueAt row 1))
        panel (s/horizontal-panel :items [edit-btn complete-btn delete-btn])]

    ;; Style des boutons
    (.setBackground edit-btn (Color/decode warning-color))
    (.setForeground edit-btn Color/WHITE)
    (.setBackground complete-btn (Color/decode success-color))
    (.setForeground complete-btn Color/WHITE)
    (.setBackground delete-btn (Color/decode danger-color))
    (.setForeground delete-btn Color/WHITE)

    ;; Actions des boutons
    (.addActionListener edit-btn
                        (proxy [ActionListener] []
                          (actionPerformed [_]
                            (let [title (-> table (.getValueAt row 2))
                                  desc (-> table (.getValueAt row 3))
                                  edit-dialog (create-edit-dialog current-id title desc todos-table)]
                              (.setVisible edit-dialog true)))))

    (.addActionListener complete-btn
                        (proxy [ActionListener] []
                          (actionPerformed [_]
                            (model/complete-todo! current-id)
                            (model/update-table-from-state! todos-table))))

    (.addActionListener delete-btn
                        (proxy [ActionListener] []
                          (actionPerformed [_]
                            (model/delete-todo! current-id)
                            (model/update-table-from-state! todos-table))))

    ;; Retourner le panneau avec les boutons
    panel))

;; Renderer pour les boutons d'action
(defn create-action-renderer [table todos-table]
  (proxy [TableCellRenderer] []
    (getTableCellRendererComponent [table value isSelected hasFocus row column]
      (let [panel (create-action-panel table todos-table row)]
        (.setBackground panel (if isSelected
                                (Color/decode "#e0e0e0")
                                (if (even? row)
                                  (Color/decode "#f8f8f8")
                                  (Color/decode "#ffffff"))))
        panel))))

;; Editor pour les boutons d'action
(defn create-action-editor [table todos-table]
  (let [panel (atom nil)
        current-row (atom 0)]
    (proxy [AbstractCellEditor TableCellEditor] []
      (getTableCellEditorComponent [table value isSelected row column]
        (reset! current-row row)
        (reset! panel (create-action-panel table todos-table row))
        @panel)

      (getCellEditorValue []
        "")  ;; Valeur vide car nous ne modifions pas réellement la cellule

      (isCellEditable [_]
        true))))

;; Configuration de la table
(defn setup-table [table todos-table]
  ;; Définir la hauteur des lignes
  (.setRowHeight table 40)

  ;; Configurer les renderers pour chaque type de colonne
  (.setDefaultRenderer table Boolean (checkbox-renderer))
  (.setDefaultRenderer table Object (proxy [DefaultTableCellRenderer] []
                                      (getTableCellRendererComponent [table value isSelected hasFocus row column]
                                        (let [c (proxy-super getTableCellRendererComponent table value isSelected hasFocus row column)]
                                          (when (and (= column 4) (= value true))  ;; Colonne "Terminé"
                                            (.setForeground c (Color/decode success-color)))
                                          (when (not isSelected)
                                            (.setBackground c (if (even? row)
                                                                (Color/decode "#f8f8f8")
                                                                (Color/decode "#ffffff"))))
                                          c))))

  ;; Configurer les colonnes
  (let [column-model (.getColumnModel table)]
    ;; Colonne de sélection
    (-> column-model (.getColumn 0) (.setMaxWidth 80))
    ;; Colonne ID (peut être cachée si besoin)
    (-> column-model (.getColumn 1) (.setMaxWidth 50))
    ;; Colonne Actions avec renderer et editor
    (let [action-column (-> column-model (.getColumn 6))]
      (.setCellRenderer action-column (create-action-renderer table todos-table))
      (.setCellEditor action-column (create-action-editor table todos-table)))))

;; Création d'une boîte de dialogue pour l'édition
(defn create-edit-dialog [todo-id title desc todos-table]
  (let [dialog (s/dialog :title "Modifier la tâche"
                         :content (s/vertical-panel
                                   :items [(s/label :text "Titre:")
                                           (s/text :id :title-field :text title)
                                           (s/label :text "Description:")
                                           (s/text :id :desc-field :text desc)
                                           (s/button :text "Enregistrer"
                                                     :listen [:action
                                                              (fn [event]
                                                                (let [new-title (s/text (s/select (s/to-root event) [:#title-field]))
                                                                      new-desc (s/text (s/select (s/to-root event) [:#desc-field]))]
                                                               ;; Appel à la fonction de mise à jour
                                                                  (model/update-todo! todo-id new-title new-desc)
                                                               ;; Mettre à jour la table
                                                                  (model/update-table-from-state! todos-table)
                                                               ;; Fermer la boîte de dialogue
                                                                  (.dispose (s/to-root event))))])])
                         :size [400 :by 250])]
    dialog))

;; Composants UI améliorés
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
        add-button (styled-button "Ajouter" primary-color)]

    {:panel (s/border-panel
             :center (s/horizontal-panel
                      :items [title-input desc-input add-button]
                      :border (border/compound-border
                               (border/empty-border :thickness 15)
                               (border/line-border :color "#e0e0e0" :thickness 1))
                      :background (Color/decode card-bg))
             :background (Color/decode bg-color))
     :title-input title-input
     :desc-input desc-input
     :add-button add-button}))

(defn create-todo-table []
  (let [model (create-table-model)
        table (s/table
               :model model
               :font (font/font :name "Arial" :size 14)
               :show-grid? true
               :background (Color/decode card-bg))]
    table))

;; Handler pour le bouton Ajouter
(defn setup-add-handler [title-input desc-input add-button todos-table]
  (s/listen add-button :action
            (fn [_]
              (model/add-todo!
               (s/text title-input)
               (s/text desc-input))
              (model/update-table-from-state! todos-table)
              (s/text! title-input "")
              (s/text! desc-input ""))))

;; Function pour ajouter le checkbox "Tout sélectionner"
(defn setup-select-all [todos-table select-all-checkbox]
  (s/listen select-all-checkbox :action
            (fn [_]
              (let [selected (.isSelected select-all-checkbox)
                    model (.getModel todos-table)
                    row-count (.getRowCount model)]
                (dotimes [i row-count]
                  (.setValueAt model selected i 0))))))

;; Main UI creation function
(defn create-main-frame []
  (setup-look-and-feel)

  (let [input-panel (create-input-panel)
        todos-table (create-todo-table)
        select-all-checkbox (JCheckBox. "Tout")
        title-label (s/label
                     :text "Gestionnaire de Tâches"
                     :font (font/font :name "Arial" :style :bold :size 24)
                     :foreground (Color/decode primary-color))
        title-panel (s/horizontal-panel
                     :items [title-label]
                     :border (border/empty-border :thickness 20)
                     :background (Color/decode bg-color))
        select-panel (s/horizontal-panel
                      :items [select-all-checkbox]
                      :border (border/empty-border :thickness 10)
                      :background (Color/decode bg-color))
        content-panel (s/vertical-panel
                       :items [(:panel input-panel)
                               select-panel
                               (s/scrollable todos-table
                                             :border (border/compound-border
                                                      (border/empty-border :thickness 15)
                                                      (border/line-border :color "#e0e0e0" :thickness 1)))]
                       :background (Color/decode bg-color))
        main-panel (s/border-panel
                    :north title-panel
                    :center content-panel
                    :background (Color/decode bg-color))]

    ;; Setup handlers
    (setup-add-handler
     (:title-input input-panel)
     (:desc-input input-panel)
     (:add-button input-panel)
     todos-table)

    (setup-select-all todos-table select-all-checkbox)

    ;; Setup table avec configuration complète
    (setup-table todos-table todos-table)

    ;; Initialize table
    (model/update-table-from-state! todos-table)

    ;; Create main frame 
    (s/frame
     :title "TodoList Clojure"
     :content main-panel
     :width 800
     :height 500
     :on-close :exit)))