(ns clojure-todolist.core-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-todolist.database :as db]
            [clojure-todolist.state :as state]
            [clojure-todolist.model :as model]))

;; Configuration pour les tests avec une base de données en mémoire
(defn setup-test-db [f]
  ;; Remplacer temporairement la base de données par une version en mémoire
  (with-redefs [db/db-spec {:dbtype "sqlite" :dbname ":memory:"}]
    (db/init-db!)
    (f)))

;; Utiliser cette fixture pour tous les tests
(use-fixtures :each setup-test-db)

(deftest initialization-test
  (testing "Database initialization"
    (let [todos (db/get-todos)]
      (is (vector? todos))
      (is (= 0 (count todos))))))

(deftest state-management-test
  (testing "State refreshing"
    (state/refresh-data!)
    (is (= 0 (count (state/get-todos))))

    ;; Ajouter une tâche
    (db/add-todo! "Test Task" "Test Description")
    ;; Vérifier que l'état est actualisé
    (state/refresh-data!)
    (is (= 1 (count (state/get-todos))))))

(deftest model-operations-test
  (testing "Model operations should update state"
    ;; Vérifier l'état initial
    (state/refresh-data!)
    (is (= 0 (count (state/get-todos))))

    ;; Ajouter une tâche via le modèle
    (model/add-todo! "Model Test" "Testing the model layer")

    ;; Vérifier que l'état est actualisé
    (is (= 1 (count (state/get-todos))))

    ;; Marquer comme terminé
    (let [todo-id (-> (state/get-todos) first :id)]
      (model/complete-todo! todo-id)

      ;; Vérifier que la tâche est marquée comme terminée
      (is (-> (state/get-todos) first :completed)))

    ;; Supprimer la tâche
    (let [todo-id (-> (state/get-todos) first :id)]
      (model/delete-todo! todo-id)

      ;; Vérifier que la tâche est supprimée
      (is (= 0 (count (state/get-todos)))))))
