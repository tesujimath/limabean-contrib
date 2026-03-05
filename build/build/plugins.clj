(ns build.plugins
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [limabean.adapter.beanfile :as beanfile]
            [limabean.adapter.loader :as loader]
            [zprint.core :refer [zprint]]))

(def PLUGIN-TESTS-DIR (io/file "plugin-tests"))

(defn get-plugins [] (.list PLUGIN-TESTS-DIR))

(defn get-tests-for-plugin
  [plugin]
  (->> (.list (io/file PLUGIN-TESTS-DIR plugin))
       (filter #(str/ends-with? % ".beancount"))
       (map (fn [beanfile]
              (let [name (str/replace beanfile ".beancount" "")
                    candidates [[:beancount-file beanfile]
                                [:beans-file (str name ".beans.edn")]
                                [:golden-file (str name ".golden.edn")]]]
                (into {}
                      (map (fn [[k candidate]]
                             (let [candidate-file (io/file PLUGIN-TESTS-DIR
                                                           plugin
                                                           candidate)]
                               [k
                                {:path (.getPath candidate-file),
                                 :exists (.exists candidate-file)}]))
                        candidates)))))))

(defn get-tests [] (mapcat get-tests-for-plugin (get-plugins)))

(defn create-beans
  [beancount-file beans-file]
  (let [beans (beanfile/book beancount-file)]
    (println "writing beans to" beans-file)
    (with-open [w (io/writer beans-file)] (binding [*out* w] (zprint beans)))))

(defn create-golden
  [beancount-file golden-file]
  (let [beans (loader/load-beanfile beancount-file)
        directives (:directives beans)]
    (println "writing directives to" golden-file)
    (with-open [w (io/writer golden-file)]
      (binding [*out* w] (zprint directives)))))

(defn create-tests
  [opts]
  (println "plugin tests")
  (run! (fn [{:keys [beancount-file beans-file golden-file]}]
          (when (or (not (:exists beans-file)) (:force opts))
            (create-beans (:path beancount-file) (:path beans-file)))
          (when (or (not (:exists golden-file)) (:force opts))
            (create-golden (:path beancount-file) (:path golden-file))))
        (get-tests)))
