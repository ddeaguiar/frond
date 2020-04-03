(ns frond
  (:refer-clojure :exclude [read read-string *default-data-reader-fn* *read-eval* *data-readers* *suppress-read*])
  (:use [clojure.tools.reader :only [read read-string *default-data-reader-fn* *read-eval* *data-readers* *suppress-read*]])
  (:require [clojure.java.io :as io]
            [fern.easy])
  (:import (java.io PushbackReader)))

(defn include
  [f]
  (with-open [r (io/reader (io/resource (str f)))]
    (read (PushbackReader. r))))

(def opts {:readers {'include include}})

(comment :fern
         (def e-a (fern.easy/file->environment (io/resource "a.fern")))
         (fern/evaluate e-a 'servers)

         (binding [*data-readers* (:readers opts)]
           (def e-b (fern.easy/file->environment (io/resource "b.fern"))))
         (fern/evaluate e-b 'emails)
         )

(comment :edn

         (def a (io/resource "a.edn"))
         ;; {:a #include i1.edn :b `[1 ~@#include i2.edn 4]}
         
         (def i1 (io/resource "i1.edn"))
         ;; {:foo "bar"}
         
         (def i2 (io/resource "i2.edn"))
         ;; [2 3]
         
         (binding [*data-readers* (:readers opts)]
           (with-open [r (io/reader a)]
             (read (PushbackReader. r))))
         ;; {:a {:foo "bar"}, :b [1 2 3 4]}

         (binding [*data-readers* (:readers opts)]
           (eval (read-string "{:a #include i1.edn :b `[1 ~@#include i2.edn 4]}")))
         ;; {:a {:foo "bar"}, :b [1 2 3 4]}

         (binding [*data-readers* (:readers opts)]
           (with-open [r (io/reader a)]
             (read (PushbackReader. r))
             ))
         )
