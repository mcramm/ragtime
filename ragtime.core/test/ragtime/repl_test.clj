(ns ragtime.repl-test
  (:require [clojure.test :refer :all]
            [ragtime.core-test :refer [in-memory-db assoc-migration]]
            [ragtime.repl :as repl]
            [ragtime.core :as core]))

(def migrations
  [(assoc-migration "a" :a 1)
   (assoc-migration "b" :b 2)
   (assoc-migration "c" :c 3)])

(deftest test-repl-functions
  (let [database (in-memory-db)
        config   {:datastore database :migrations migrations}]
    (is (= @(:data database) {:migrations #{}}))
    (is (= (with-out-str (repl/migrate config))
           "Applying a\nApplying b\nApplying c\n"))
    (is (= 1 (-> database :data deref :a)))
    (is (= 2 (-> database :data deref :b)))
    (is (= 3 (-> database :data deref :c)))
    (is (= (with-out-str (repl/rollback config))
           "Rolling back c\n"))
    (is (= 1 (-> database :data deref :a)))
    (is (= 2 (-> database :data deref :b)))
    (is (nil? (-> database :data deref :c)))
    (is (= (with-out-str (repl/rollback config 2))
           "Rolling back b\nRolling back a\n"))
    (is (nil? (-> database :data deref :a)))
    (is (nil? (-> database :data deref :b)))
    (is (nil? (-> database :data deref :c)))))

(deftest test-custom-reporter
  (let [database (in-memory-db)
        config   {:datastore database :migrations migrations :reporter prn}]
    (is (= @(:data database) {:migrations #{}}))
    (is (= (with-out-str (repl/migrate config))
           ":up \"a\"\n:up \"b\"\n:up \"c\"\n"))))
