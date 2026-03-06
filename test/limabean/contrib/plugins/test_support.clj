(ns limabean.contrib.plugins.test-support
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def PLUGIN-TESTS-DIR (io/file "plugin-tests"))

(defn- get-plugins [] (.list PLUGIN-TESTS-DIR))

(defn- get-tests-for-plugin
  [plugin]
  (->> (.list (io/file PLUGIN-TESTS-DIR plugin))
       (filter #(str/ends-with? % ".beancount"))
       (map
         (fn [beanfile]
           (let [test-name (str/replace beanfile ".beancount" "")
                 test-specs [[:beancount-file beanfile]
                             [:beans-file (str test-name ".beans.edn")]
                             [:golden-file (str test-name ".golden.edn")]]
                 test-specs-with-existence
                   (map (fn [[k test-spec]]
                          (let [test-spec-file
                                  (io/file PLUGIN-TESTS-DIR plugin test-spec)]
                            [k
                             {:path (.getPath test-spec-file),
                              :exists (.exists test-spec-file)}]))
                     test-specs)
                 named-tests (concat test-specs-with-existence
                                     [[:test-name test-name] [:plugin plugin]])]
             (into {} named-tests))))))

(defn get-tests [] (mapcat get-tests-for-plugin (get-plugins)))
