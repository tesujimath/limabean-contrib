(ns limabean.contrib.plugins.plugins-test
  (:require [clojure.test :refer [deftest]]
            [limabean.test]))

(def PLUGIN-TEST-ROOT "src")

(deftest plugin-tests (limabean.test/loader-tests PLUGIN-TEST-ROOT))
