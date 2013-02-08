(ns storm.starter.test.jvm.TridentWorkCountTopology
  (:use [clojure.test])
  (:require [backtype.storm [testing :as t]])
  (:import [storm.starter.trident TridentWordCount])
  (:use [storm.trident testing])
  (:use [backtype.storm util])
  )

(bootstrap-imports)


(deftest test-word-trident
    (t/with-local-cluster [cluster]
      (with-drpc [drpc]
        (letlocals
          (bind feeder (feeder-spout ["sentence"]))
          (bind topo (TridentWordCount/buildTopology feeder drpc)) 
          (with-topology [cluster topo]
              (feed feeder [["hello the man said"] ["the"]])
              (is (= [[2]] (exec-drpc drpc "words" "the")))
              (is (= [[1]] (exec-drpc drpc "words" "hello")))
              (feed feeder [["the man on the moon"] ["where are you"]])
              (is (= [[4]] (exec-drpc drpc "words" "the")))
              (is (= [[2]] (exec-drpc drpc "words" "man")))
              (is (= [[8]] (exec-drpc drpc "words" "man where you the")))
            )
          ) 
        )
      )
  )


