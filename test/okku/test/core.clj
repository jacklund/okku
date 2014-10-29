(ns okku.test.core
  (:use [okku.core])
  (:use [clojure.test]))

(deftest test-!
  (are [x y] (= (macroexpand-1 (quote x)) y)
       (okku.core/! msg) '(.tell (.getSender this) msg (.getSelf this))
       (okku.core/! target msg) '(.tell target msg (.getSelf this))))

(deftest test-spawn
  (are [x y] (= (macroexpand-1 (quote x)) y)
       (okku.core/spawn act) '(.actorOf (.getContext this) act)
       (okku.core/spawn act :in asys :router router :name name)
       '(.actorOf asys (okku.core/with-router act router) name)
       (okku.core/spawn act :deploy-on addr)
       '(.actorOf
          (.getContext this)
          (okku.core/with-deploy act addr))))

(deftest test-dispatch-on
  (are [x y] (= (macroexpand-1 x) y)
       '(okku.core/dispatch-on t
                               :dv1 (answer1)
                               :dv2 (answer2))
       '(clojure.core/cond (clojure.core/= t :dv1) (answer1)
              (clojure.core/= t :dv2) (answer2)
              :else (.unhandled this t))))

(deftest test-actor
  (is (= akka.actor.Props (type (actor (onReceive []))))))
