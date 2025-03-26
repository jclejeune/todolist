(ns clojure-todolist.database
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]))

;; Configuration de la connexion à la base de données
(def db-spec
  {:dbtype "sqlite"
   :dbname "todolist.db"})

;; Fonction pour initialiser la base de données
(defn init-db! []
  (let [conn (jdbc/get-connection db-spec)]
    (jdbc/execute! conn
                   ["CREATE TABLE IF NOT EXISTS todos (
         id INTEGER PRIMARY KEY AUTOINCREMENT,
         title TEXT NOT NULL,
         description TEXT,
         completed BOOLEAN DEFAULT 0,
         created_at DATETIME DEFAULT CURRENT_TIMESTAMP
       )"])))

;; Fonction pour ajouter une tâche
(defn add-todo! [title description]
  (let [result (sql/insert! db-spec :todos
                            {:title title
                             :description description
                             :completed false})]
    (get result :id)))

;; Fonction pour récupérer toutes les tâches avec date formatée
(defn get-todos []
  (sql/query db-spec
             ["SELECT id, title, description, completed, 
               strftime('%d/%m/%Y %H:%M', created_at) as created_at 
               FROM todos ORDER BY created_at DESC"]
             {:builder-fn rs/as-unqualified-maps}))

;; Fonction pour marquer une tâche comme terminée
(defn complete-todo! [id]
  (sql/update! db-spec :todos
               {:completed true}
               {:id id}))

;; Fonction pour supprimer une tâche
(defn delete-todo! [id]
  (sql/delete! db-spec :todos {:id id}))

;; Fonction pour mettre à jour une tâche
(defn update-todo! [id title description]
  (sql/update! db-spec :todos
               {:title title
                :description description}
               {:id id}))