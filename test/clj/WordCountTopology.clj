(ns storm.starter.test.jvm.WordCountTopology
  (:use [clojure.test])
  (:import [storm.starter WordCountTopology])
  (:use [backtype.storm bootstrap testing])
  (:use [backtype.storm.daemon common])
  )

(bootstrap)

(defn- word-count-p
  [input output]
  (is (=
        (frequencies
          (reduce
            (fn [acc sentence]
              (concat acc (.split (first sentence) " ")))
            []
            input))
        (reduce
          (fn [m [word n]]
            (assoc m word n))
          {}
          output))))


(deftest test-word-count
    (with-simulated-time-local-cluster [cluster :supervisors 4]
      (let [ topology (WordCountTopology/makeTopology)
             results (complete-topology 
                       cluster
                       topology
                       :mock-sources {"spout" [["little brown dog"]
                                           ["petted the dog"]
                                           ["petted a badger"]]}
                       :storm-conf {TOPOLOGY-DEBUG true
                                    TOPOLOGY-WORKERS 2}) ]
        ; test initial case
        (word-count-p [] [])
        ; test after run
        (word-count-p
          (read-tuples results "spout")
          (read-tuples results "count")))))

