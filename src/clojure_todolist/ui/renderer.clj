(ns clojure-todolist.ui.renderers
  (:require [seesaw.core :as s]
            [clojure-todolist.ui.styles :as styles]
            [clojure-todolist.model :as model])
  (:import [javax.swing JButton JCheckBox AbstractCellEditor]
           [javax.swing.table DefaultTableModel DefaultTableCellRenderer TableCellRenderer TableCellEditor]
           [java.awt Color]
           [java.awt.event ActionListener]))

;; Déclaration préalable
(declare create-edit-dialog)

;; Modèle de table amélioré
(defn create-table-model []
  (proxy [DefaultTableModel] [(into-array ["Sélection" "ID" "Titre" "Description" "Terminé" "Date" "Actions"])
                              0]
    (isCellEditable [row col]
      (or (= col 0) (= col 6)))))

;; Renderer pour les checkboxes
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

;; Panneau d'actions
(defn create-action-panel [table todos-table row]
  (let [edit-btn (JButton. "Modifier")
        complete-btn (JButton. "Terminé")
        delete-btn (JButton. "Supprimer")
        current-id (-> table (.getValueAt row 1))
        panel (s/horizontal-panel :items [edit-btn complete-btn delete-btn])]

    ;; Style des boutons
    (.setBackground edit-btn (Color/decode styles/warning-color))
    (.setForeground edit-btn Color/WHITE)
    (.setBackground complete-btn (Color/decode styles/success-color))
    (.setForeground complete-btn Color/WHITE)
    (.setBackground delete-btn (Color/decode styles/danger-color))
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
        "")

      (isCellEditable [_]
        true))))