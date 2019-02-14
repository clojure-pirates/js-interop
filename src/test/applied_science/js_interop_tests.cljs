(ns applied-science.js-interop-tests
  (:require [applied-science.js-interop :as j]
            [cljs.test :as test :refer [is
                                        are
                                        testing
                                        deftest]]
            [clojure.pprint :refer [pprint]]))

(defn clj= [& args]
  (->> args
       (mapv #(js->clj % :keywordize-keys true))
       (apply =)))

(deftest js-interop

  (testing "get-in"
    (are [macro-expr fn-expr val]
      (clj= macro-expr fn-expr val)


      ;; get with nil
      (j/get nil :x)
      (apply j/get [nil :x])
      nil

      ;; get with default
      (j/get nil :x 10)
      (apply j/get [nil :x 10])
      10

      ;; lookup semantics for default with nil-present
      (j/get #js{:x nil} :x 10)
      (apply j/get [#js{:x nil} :x 10])
      nil


      ;; get-in
      (j/get-in nil [:x])
      (apply j/get-in [nil [:x]])
      nil

      ;; get-in with default
      (j/get-in nil [:x] 10)
      (apply j/get-in [nil [:x] 10])
      10

      ;; get-in lookup semantics with nil-present
      (j/get-in #js{:x nil} [:x] 10)
      (apply j/get-in [#js{:x nil} [:x] 10])
      nil

      (j/get-in #js {:x 10} [:x] 20)
      (apply j/get-in [#js {:x 10} [:x] 20])
      10

      ;; get-in multi-level
      (j/get-in #js {:x #js {:y 10}} [:x :y])
      (apply j/get-in [#js {:x #js {:y 10}} [:x :y]])
      10

      ;; get-in with nested not-present
      (j/get-in #js {:x #js {}} [:x :y])
      (apply j/get-in [#js {:x #js {}} [:x :y]])
      nil

      ;; get-in with nested nil-present
      (j/get-in #js {:x #js {:y nil}} [:x :y] 10)
      (apply j/get-in [#js {:x #js {:y nil}} [:x :y] 10])
      nil

      ;; get-in with array
      (j/get-in #js [#js {:x 10}] [0 :x])
      (apply j/get-in [#js [#js {:x 10}] [0 :x]])
      10


      ;; assoc-in
      (j/assoc-in! #js {} [:x :y] 10)
      (apply j/assoc-in! [#js {} [:x :y] 10])
      {:x {:y 10}}

      ;; assoc-in with nil
      (j/assoc-in! nil [:x :y] 10)
      (apply j/assoc-in! [nil [:x :y] 10])
      {:x {:y 10}}

      ;; assoc-in with nested not-present
      (j/assoc-in! #js {:x #js {}} [:x :y] 10)
      (apply j/assoc-in! [#js {:x #js {}} [:x :y] 10])
      {:x {:y 10}}

      ;; assoc-in with nested nil
      (j/assoc-in! #js {:x #js {:y nil}} [:x :y] 10)
      (apply j/assoc-in! [#js {:x #js {:y nil}} [:x :y] 10])
      {:x {:y 10}}

      ;; update with f
      (j/update! #js {:x 9} :x inc)
      (apply j/update! [#js {:x 9} :x inc])
      {:x 10}

      ;; update with f and args
      (j/update! #js {:x 0} :x + 1 9)
      (apply j/update! [#js {:x 0} :x + 1 9])
      {:x 10}

      ;; update an array
      (j/update! #js [10] 0 inc)
      (apply j/update! [#js [10] 0 inc])
      [11]

      ;; update nil
      (j/update! nil :x (fnil inc 9))
      (apply j/update! [nil :x (fnil inc 9)])
      {:x 10}

      ;; update-in nil
      (j/update-in! nil [:x :y] (fnil inc 0))
      (apply j/update-in! [nil [:x :y] (fnil inc 0)])
      {:x {:y 1}}

      ;; update-in with args
      (j/update-in! nil [:x :y] (fnil + 0) 10)
      (apply j/update-in! [nil [:x :y] (fnil + 0) 10])
      {:x {:y 10}}

      ;; update-in mutates provided object
      (j/update-in! #js {:x 0
                         :y 9} [:y] inc)
      (apply j/update-in! [#js {:x 0
                                :y 9} [:y] inc])
      {:x 0
       :y 10}


      ;; lookup
      (let [{:keys [a b c]} (j/lookup #js {:a 1
                                           :b 2
                                           :c 3})]
        [a b c])
      ((juxt :a :b :c) (apply j/lookup [#js {:a 1
                                             :b 2
                                             :c 3}]))
      [1 2 3]


      ;; select-keys
      (j/select-keys #js {:x 10} [:x :y])
      (apply j/select-keys [#js {:x 10} [:x :y]])
      {:x 10}

      ;; select-keys with nil
      (j/select-keys nil [:x])
      (apply j/select-keys [nil []])
      {}


      ;; array ops

      (j/push! #js [0] 10)
      (apply j/push! [#js [0] 10])
      [0 10]

      (j/unshift! #js [0] 10)
      (apply j/unshift! [#js [0] 10])
      [10 0]

      (j/call #js [10] :indexOf 10)
      (apply j/call [#js [10] :indexOf 10])
      0)

    (is (thrown? js/Error
                 (j/assoc-in! #js {} [] 10))
        "Empty paths for mutations are not accepted,
         JavaScript objects cannot have nil as a key")))