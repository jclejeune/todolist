(ns clojure-todolist.model
  (:require [clojure-todolist.database :as db]
            [clojure-todolist.state :as state]))


;; Opérations du modèle qui connectent DB et State
(defn add-todo! [title description]
  (db/add-todo! title description)
  (state/refresh-data!))

(defn complete-todo! [id]
  (db/complete-todo! id)
  (state/refresh-data!))

(defn delete-todo! [id]
  (db/delete-todo! id)
  (state/refresh-data!))


(defn update-table-from-state! [table]
  (let [model (.getModel table)
        todos (state/get-todos)]
    ;; Nettoyer le modèle existant
    (while (> (.getRowCount model) 0)
      (.removeRow model 0))

    ;; Ajouter chaque tâche avec checkbox et boutons d'action
    (doseq [todo todos]
      (.addRow model
               (object-array
                [false  ;; Checkbox de sélection (non coché par défaut)
                 (get todo :id)
                 (get todo :title)
                 (get todo :description)
                 (get todo :completed)
                 (get todo :created_at)
                 ""]))))) ;; Colonne pour les actions (sera remplacée par les boutons)

;; Cette fonction sera appelée depuis ui.clj quand un utilisateur modifie une tâche
  (defn update-todo! [id title description]
    (db/update-todo! id title description)
    (state/refresh-data!))