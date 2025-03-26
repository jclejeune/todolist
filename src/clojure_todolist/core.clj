(ns clojure-todolist.core
  (:require [seesaw.core :as s]
            [clojure-todolist.database :as db]
            [clojure-todolist.state :as state]
            [clojure-todolist.ui.core :as ui])
  (:gen-class))

;; Fonction principale
(defn -main [& _]
  ;; Initialisation
  (db/init-db!)
  (state/refresh-data!)

  ;; Création et affichage de la fenêtre
  (s/invoke-later
   (-> (ui/create-main-frame)
       s/pack!
       s/show!)))