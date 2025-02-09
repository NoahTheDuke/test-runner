(ns cognitect.test-runner.api
  (:refer-clojure :exclude [test])
  (:require
    [cognitect.test-runner :as tr]))

(defn- do-test
  [{:keys [dirs nses patterns vars includes excludes outputs]}]
  (let [adapted {:dir (when (seq dirs) (set dirs))
                 :namespace (when (seq nses) (set nses))
                 :namespace-regex (when (seq patterns) (map re-pattern patterns))
                 :var (when (seq vars) (set vars))
                 :include (when (seq includes) (set includes))
                 :exclude (when (seq excludes) (set excludes))
                 :output (-> (mapv #(if (qualified-symbol? %)
                                      %
                                      (symbol "lazytest.reporters"
                                              (name %)))
                                   outputs)
                             (not-empty)
                             (or ['lazytest.reporters/nested]))}]
    (tr/test adapted)))

(defn test
  "Invoke the test-runner with the following options:

  * :dirs - coll of directories containing tests, default= [\"test\"]
  * :nses - coll of namespace symbols to test
  * :patterns - coll of regex strings to match namespaces (clojure.test-only)
  * :vars - coll of fully qualified symbols to run tests on
  * :includes - coll of test metadata keywords to include
  * :excludes - coll of test metadata keywords to exclude
  * :outputs - coll of LazyTest-only output reporters to use

  If neither :nses nor :patterns is supplied, use `:patterns [\".*-test$\"]`."
  [opts]
  (let [{:keys [fail error]} (do-test opts)]
    (when (> (+ fail error) 0)
      (throw (ex-info "Test failures or errors occurred." {})))))
