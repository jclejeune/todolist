(ns clojure-todolist.core
  (:require [seesaw.core :as s]
            [clojure-todolist.database :as db])
  (:import [javax.swing.table DefaultTableModel])
  (:gen-class))

;; Atome pour stocker les tâches
(def todos-data (atom (db/get-todos)))

(defn refresh-todos-data! []
  (reset! todos-data (db/get-todos)))


;; Fonction pour actualiser la table des tâches
(defn refresh-todos-table! [table]
  (refresh-todos-data!) ;; Mettez à jour l'atome en premier
  (let [todos @todos-data ;; Utilisez l'atome mis à jour
        model (.getModel table)]
    ;; Nettoyer le modèle existant
    (while (> (.getRowCount model) 0)
      (.removeRow model 0))

    ;; Ajouter chaque tâche
    (doseq [todo todos]
      (.addRow model
               (object-array
                [(get todo :id)
                 (get todo :title)
                 (get todo :description)
                 (get todo :completed)])))))

;; Fonction pour créer le modèle de table initial
(defn create-table-model []
  (let [column-names ["ID" "Titre" "Description" "Terminé"]
        model (DefaultTableModel. (into-array column-names) 0)]
    model))
;; Création de la fenêtre principale
(defn create-main-frame []
  (let [title-input (s/text :text "Titre de la tâche")
        desc-input (s/text :text "Description")

        ;; Table pour afficher les tâches
        todos-table (s/table
                     :model (create-table-model))

        ;; Bouton pour ajouter une tâche
        add-button (s/button
                    :text "Ajouter Tâche"
                    :listen [:action
                             (fn [_]
                               (db/add-todo!
                                (s/text title-input)
                                (s/text desc-input))
                               (refresh-todos-table! todos-table)
                               (s/text! title-input "")
                               (s/text! desc-input ""))])

        ;; Bouton pour marquer comme terminé
        complete-button (s/button
                         :text "Marquer Terminé"
                         :listen [:action
                                  (fn [_]
                                    (when-let [selected-row (.getSelectedRow todos-table)]
                                      (when (>= selected-row 0)
                                        (let [todo-id (-> todos-table
                                                          .getModel
                                                          (.getValueAt selected-row 0))]
                                          (db/complete-todo! todo-id)
                                          (refresh-todos-table! todos-table)))))])

        ;; Bouton pour supprimer
        delete-button (s/button
                       :text "Supprimer"
                       :listen [:action
                                (fn [_]
                                  (when-let [selected-row (.getSelectedRow todos-table)]
                                    (when (>= selected-row 0)
                                      (let [todo-id (-> todos-table
                                                        .getModel
                                                        (.getValueAt selected-row 0))]
                                        (db/delete-todo! todo-id)
                                        (refresh-todos-table! todos-table)))))])]

    ;; Fenêtre principale
    (s/frame
     :title "TodoList Clojure"
     :content (s/vertical-panel
               :items [(s/horizontal-panel
                        :items [title-input
                                desc-input
                                add-button])
                       (s/scrollable todos-table)
                       (s/horizontal-panel
                        :items [complete-button
                                delete-button])])
     :width 600
     :height 400
     :on-close :exit)))

;; Fonction principale
(defn -main [& _]
  ;; Initialisation de la base de données
  (db/init-db!)

  ;; Création et affichage de la fenêtre
  (s/invoke-later
   (-> (create-main-frame)
       s/pack!
       s/show!)))