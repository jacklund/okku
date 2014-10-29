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

(deftest test-look-up
  (let [actor-system (okku.core/actor-system "test" :local true)
        actor (okku.core/spawn (okku.core/actor (onReceive [msg])) :in actor-system :name "foo")]
    (is (= actor (okku.core/look-up "/user/foo" :in actor-system)))))

(deftest test-select-resolve-one
  (let [actor-system (okku.core/actor-system "test" :local true)
        actor (okku.core/spawn (okku.core/actor (onReceive [msg])) :in actor-system :name "foo")
        timeout (okku.core/timeout 1 seconds)]
    (is (= actor
           (okku.core/await-future
             (okku.core/resolve-one
               (okku.core/select "/user/foo" :in actor-system)
               timeout)
             timeout)))))

(deftest test-select-identify
  (let [actor-system (okku.core/actor-system "test" :local true)
        actor (okku.core/spawn (okku.core/actor (onReceive [msg] (prn msg))) :in actor-system :name "foo")
        timeout (okku.core/timeout 1 seconds)]
    (is (= actor
           (.getRef
             (okku.core/await-future
               (okku.core/identify
                 (okku.core/select "/user/foo" :in actor-system)
                 1
                 timeout)
               timeout))))))

(deftest test-!
  (let [actor-system (okku.core/actor-system "test" :local true)
        value (atom nil)
        a (okku.core/spawn (okku.core/actor (onReceive [msg] (reset! value msg))) :in actor-system :name "foo")
        b (okku.core/spawn (okku.core/actor (onReceive [msg] (! a msg))) :in actor-system :name "bar")
        msg "Foo!"]
    (okku.core/tell b msg)
    (Thread/sleep 500)
    (is (= msg @value))))
