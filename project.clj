(defproject storm-starter "0.0.1-SNAPSHOT"
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :test-paths ["test/jvm" "test/clj"]
  :javac-options {:debug "true" :fork "true"}
  :resources-path "multilang"
  :aot :all
  :repositories {
;;                 "twitter4j" "http://twitter4j.org/maven2"
                 }

  :dependencies [
;;                 [org.twitter4j/twitter4j-core "2.2.6-SNAPSHOT"]
;;                 [org.twitter4j/twitter4j-stream "2.2.6-SNAPSHOT"]
                   [commons-collections/commons-collections "3.2.1"]]

  :profiles {:dev {:dependencies [[storm "0.8.2"]
                     [org.clojure/clojure "1.4.0"]
                     [junit/junit "4.11"]
                     [org.mockito/mockito-all "1.8.4"]
                     [org.easytesting/fest-assert-core "2.0M8"]
                     [org.testng/testng "6.1.1"]]}}
  
  )

