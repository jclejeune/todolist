(ns clojure-todolist.state
  (:require [clojure-todolist.database :as db]))

;; Atome pour stocker l'état des tâches
(def todos-data (atom (db/get-todos)))

;; Fonctions pour manipuler l'état
(defn refresh-data! []
  (reset! todos-data (db/get-todos)))

(defn get-todos []
  @todos-data)