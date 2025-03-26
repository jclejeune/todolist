(defproject clojure-todolist "0.1.0-SNAPSHOT"
  :description "Une application TodoList avec Clojure et SQLite"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.xerial/sqlite-jdbc "3.36.0.3"]  ; Pilote JDBC pour SQLite
                 [com.github.seancorfield/next.jdbc "1.3.834"]
                 [seesaw "1.5.0"]  ; Pour l'interface graphique
                 [com.formdev/flatlaf "3.1.1"]]  ; Look and Feel moderne
  :main ^:skip-aot clojure-todolist.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
