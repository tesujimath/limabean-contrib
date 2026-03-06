(ns build.plugins
  (:require [clojure.java.io :as io]
            [limabean.adapter.beanfile :as beanfile]
            [limabean.adapter.loader :as loader]
            [limabean.contrib.plugins.test-support :as test-support]
            [zprint.core :refer [zprint]]))

(defn create-beans
  [beancount-file beans-file]
  (let [beans (beanfile/book beancount-file)]
    (println "writing beans to" beans-file)
    (with-open [w (io/writer beans-file)] (binding [*out* w] (zprint beans)))))

(defn create-golden
  [beancount-file golden-file]
  (let [beans (loader/load-beanfile beancount-file)
        bad-plugins (filter :err (:external (:plugins beans)))
        directives (:directives beans)]
    (if (empty? bad-plugins)
      (do (println "writing directives to" golden-file)
          (with-open [w (io/writer golden-file)]
            (binding [*out* w] (zprint directives))))
      (println "not writing directives to" golden-file
               "because bad plugins" bad-plugins))))

(defn create-tests
  [opts]
  (run! (fn [{:keys [beancount-file beans-file golden-file]}]
          (when (or (not (:exists beans-file)) (:force opts))
            (create-beans (:path beancount-file) (:path beans-file)))
          (when (or (not (:exists golden-file)) (:force opts))
            (create-golden (:path beancount-file) (:path golden-file))))
        (test-support/get-tests)))
