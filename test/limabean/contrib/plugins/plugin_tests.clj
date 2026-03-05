(ns limabean.contrib.plugins.plugin-tests
  (:require [limabean.adapter.plugins :as plugins]
            [limabean.adapter.beanfile :as beanfile]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest testing]]
            [matcho.core :as matcho]))

(def TEST-CASES-DIR "plugin-tests")

(defn- sorted-dir-entries
  "Return a sorted list of files in `dir`, an `io/file`"
  [dir]
  (let [unsorted (.list dir)] (sort (vec unsorted))))

(defn get-plugin-tests
  [plugin]
  (let [all-files (sorted-dir-entries (io/file TEST-CASES-DIR plugin))
        golden-dirs (filter #(str/ends-with? % ".golden") all-files)
        candidate-plugin-tests
          (map (fn [golden-dir]
                 (let [name (str/replace golden-dir ".golden" "")
                       beanfile (.getPath (io/file TEST-CASES-DIR
                                                   plugin
                                                   (str name ".edn")))
                       golden-output (io/file TEST-CASES-DIR
                                              plugin
                                              golden-dir
                                              "directives.edn")]
                   {:plugin plugin,
                    :name name,
                    :beanfile beanfile,
                    :golden-output golden-output}))
            golden-dirs)
        plugin-tests (filter #(.exists (:golden-output %))
                       candidate-plugin-tests)]
    plugin-tests))

(defn get-tests
  "Look for golden directories in test cases to generate test base paths"
  []
  (let [plugins (sorted-dir-entries (io/file TEST-CASES-DIR))
        plugin-tests (mapcat get-plugin-tests plugins)]
    plugin-tests))

(deftest plugin-tests
  (doseq [{:keys [plugin name beanfile golden-output]} (get-tests)]
    (println "plugin test" plugin name beanfile golden-output)
    (testing (str plugin "/" name)
      (when (.exists golden-output)
        (let [beans (plugins/resolve-external (beanfile/read-edn-string
                                                (slurp beanfile)))
              expected (beanfile/read-edn-string (slurp golden-output))
              directives (plugins/run-booked-xf (:directives beans)
                                                (:plugins beans))]
          (matcho/assert directives expected))))))
