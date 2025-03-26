(ns clojure-todolist.ui.styles
  (:import [java.awt Color]))

;; Définition des couleurs et styles
(def ^:clj-kondo/ignore primary-color "#4a86e8")
(def ^:clj-kondo/ignore success-color "#34a853")
(def ^:clj-kondo/ignore danger-color "#ea4335")
(def ^:clj-kondo/ignore warning-color "#fbbc05")
(def ^:clj-kondo/ignore bg-color "#f9f9f9")
(def ^:clj-kondo/ignore card-bg "#ffffff")

;; Fonction utilitaire pour convertir une chaîne hexadécimale en objet Color
(defn ^:clj-kondo/ignore hex->color
  "Convertit une couleur hexadécimale en objet java.awt.Color"
  [hex-code]
  (Color/decode hex-code))

;; Fonction pour obtenir une couleur par son nom
(defn ^:clj-kondo/ignore get-color [color-name]
  (case color-name
    :primary (hex->color primary-color)
    :success (hex->color success-color)
    :danger (hex->color danger-color)
    :warning (hex->color warning-color)
    :background (hex->color bg-color)
    :card-bg (hex->color card-bg)
    ;; Par défaut, retourner noir
    (Color/BLACK)))