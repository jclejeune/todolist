(ns clojure-todolist.ui.handlers
  (:require [seesaw.core :as s]
            [clojure-todolist.model :as model]))

;; Gestionnaire pour le bouton Ajouter
(defn setup-add-handler [title-input desc-input add-button todos-table]
  (s/listen add-button :action
            (fn [_]
              (model/add-todo!
               (s/text title-input)
               (s/text desc-input))
              (model/update-table-from-state! todos-table)
              (s/text! title-input "")
              (s/text! desc-input ""))))

;; Gestionnaire pour le checkbox "Tout sélectionner"
(defn setup-select-all [todos-table select-all-checkbox]
  (s/listen select-all-checkbox :action
            (fn [_]
              (let [selected (.isSelected select-all-checkbox)
                    model (.getModel todos-table)
                    row-count (.getRowCount model)]
                (dotimes [i row-count]
                  (.setValueAt model selected i 0))))))

;; Configuration de la table
(defn setup-table [table todos-table]
  (let [renderers (require 'clojure-todolist.ui.renderers)]
    ;; Importer les fonctions de renderers
    (let [checkbox-renderer (ns-resolve 'clojure-todolist.ui.renderers 'checkbox-renderer)
          create-action-renderer (ns-resolve 'clojure-todolist.ui.renderers 'create-action-renderer)
          create-action-editor (ns-resolve 'clojure-todolist.ui.renderers 'create-action-editor)]

      ;; Hauteur des lignes
      (.setRowHeight table 40)

      ;; Renderer pour les booléens
      (.setDefaultRenderer table Boolean (checkbox-renderer))

      ;; Renderer pour les autres cellules
      (.setDefaultRenderer table Object
                           (proxy [DefaultTableCellRenderer] []
                             (getTableCellRendererComponent [table value isSelected hasFocus row column]
                               (let [c (proxy-super getTableCellRendererComponent table value isSelected hasFocus row column)]
                                 (when (and (= column 4) (= value true))
                                   (.setForeground c (Color/decode "#34a853")))
                                 (when (not isSelected)
                                   (.setBackground c (if (even? row)
                                                       (Color/decode "#f8f8f8")
                                                       (Color/decode "#ffffff"))))
                                 c))))

      ;; Configuration des colonnes
      (let [column-model (.getColumnModel table)]
        (-> column-model (.getColumn 0) (.setMaxWidth 80))
        (-> column-model (.getColumn 1) (.setMaxWidth 50))
        (let [action-column (-> column-model (.getColumn 6))]
          (.setCellRenderer action-column (create-action-renderer table todos-table))
          (.setCellEditor action-column (create-action-editor table todos-table)))))))