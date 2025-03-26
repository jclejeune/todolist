(ns clojure-todolist.ui.dialogs
  (:require [seesaw.core :as s]
            [clojure-todolist.model :as model]))

;; Fenêtre d'édition de tâche
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
                                                                  (model/update-todo! todo-id new-title new-desc)
                                                                  (model/update-table-from-state! todos-table)
                                                                  (.dispose (s/to-root event))))])])
                         :size [400 :by 250])]
    dialog))