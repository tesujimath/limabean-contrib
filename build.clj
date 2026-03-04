(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]
            [deps-deploy.deps-deploy :as deps-deploy]))

(def lib 'io.github.tesujimath/limabean-contrib)
(def version "0.1.0-SNAPSHOT")
(def main 'limabean.contrib.main)
(def class-dir "target/classes")
;; mvn-local-repo must be an absolute path outside of clj
;; so we can test install the jar without access to local deps.edn
(def mvn-local-repo
  (.getPath (io/file (System/getProperty "user.dir") ".." "target" "m2")))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(def basis (b/create-basis {:project "deps.edn"}))


(defn- pom-template
  [version]
  [[:description
    "A supplementary package for limabean which welcomes contributions."]
   [:url "https://github.com/tesujimath/limabean-contrib"]
   [:licenses
    [:license [:name "Apache License, Version 2.0"]
     [:url "https://www.apache.org/licenses/LICENSE-2.0"]]
    [:license [:name "MIT license"]
     [:url "https://opensource.org/licenses/MIT"]]]
   [:developers
    [:developer [:name "Simon Guest"] [:email "simon.guest@tesujimath.org"]
     [:url "https://github.com/tesujimath"]]]
   [:scm [:url "https://github.com/tesujimath/limabean-contrib"]
    [:connection "scm:git:git://github.com/tesujimath/limabean-contrib.git"]
    [:developerConnection
     "scm:git:ssh://git@github.com/tesujimath/limabean-contrib.git"]
    [:tag version]]])

(defn test
  "Run all the tests."
  [opts]
  (let [cmds (b/java-command {:basis basis,
                              :main 'clojure.main,
                              :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn clean [opts] (b/delete {:path "target"}) opts)

(defn write-pom
  "Write pom.xml from template"
  [opts]
  (b/write-pom (assoc opts
                 :class-dir class-dir
                 :lib lib
                 :version version
                 :basis basis
                 :src-dirs ["src"]
                 :src-pom :none
                 :pom-data (pom-template version)))
  (let [generated-pom-file (format "target/classes/META-INF/maven/%s/pom.xml"
                                   lib)
        committed-pom-file "pom.xml"]
    (println "wrote" generated-pom-file)
    (b/copy-file {:src generated-pom-file, :target committed-pom-file})
    (println "copied" generated-pom-file "to" committed-pom-file)
    (assoc opts :pom-file generated-pom-file)))

(defn- jar-opts
  [opts]
  (assoc opts
    :class-dir class-dir
    :jar-file jar-file
    :manifest {"Implementation-Version" version}))

(defn jar
  [opts]
  (let [opts (clean opts)
        opts (write-pom opts)
        opts (jar-opts opts)]
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"], :target-dir class-dir})
    (println "\nBuilding jar" (:jar-file opts))
    (b/jar opts)
    opts))

(defn install
  "Install the JAR and pom into `mvn-local-repo` for testing"
  [opts]
  (let [opts (jar opts)
        basis {:mvn/local-repo mvn-local-repo}]
    (println "Installing jarfile using basis" basis)
    (b/install (assoc opts
                 :basis basis
                 :lib lib
                 :version version))
    opts))

(defn deploy
  [opts]
  (let [opts (jar opts)]
    (let [artifact (:jar-file opts)
          pom-file (:pom-file opts)]
      (println "deploying pom-file" pom-file "artifact" artifact)
      (deps-deploy/deploy {:installer :remote,
                           :sign-releases true,
                           :artifact artifact,
                           :pom-file pom-file}))
    opts))
