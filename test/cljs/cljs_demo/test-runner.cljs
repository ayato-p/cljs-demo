(ns cljs_demo.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [cljs_demo.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'cljs_demo.core-test))
    0
    1))
