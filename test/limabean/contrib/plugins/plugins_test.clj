(ns limabean.contrib.plugins.plugins-test
  (:require [clojure.test :refer [deftest is testing]]
            [limabean.adapter.plugins :as plugins]
            [limabean.adapter.beanfile :as beanfile]
            [limabean.contrib.plugins.test-support :as test-support]
            [matcho.core :as matcho]
            [clojure.string :as str]))

(deftest plugin-tests
  (doseq [{:keys [plugin test-name beans-file golden-file]}
            (test-support/get-tests)]
    (when (and (:exists beans-file) (:exists golden-file))
      (testing (str plugin "/" test-name)
        (let [beans (plugins/resolve-external (beanfile/read-edn-string
                                                (slurp (:path beans-file))))
              bad-plugins (filter :err (:external (:plugins beans)))
              expected (beanfile/read-edn-string (slurp (:path golden-file)))
              {:keys [directives err]}
                (plugins/run-booked-xf (:directives beans) (:plugins beans))]
          (is (empty? bad-plugins) (str/join " " (map :name bad-plugins)))
          (matcho/assert expected directives)
          (is (nil? err)))))))
