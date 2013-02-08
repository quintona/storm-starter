# Changes from original

I have made a few changes/upgrades to the start project:

1. I have added the original clojure integration tests from: https://github.com/schleyfox/storm-starter, but just for the word count example so that I can use that as a stable based for further integration tests using clojure.
2. I have also upgraded so that the project works with lein 2.
3. I have implemented a trident integration test of the trident word count topology. This is based on https://github.com/nathanmarz/storm/blob/master/test/clj/storm/trident/integration_test.clj, but this test will exercise the java defined topology. 

# Example Storm topologies

storm-starter contains a variety of examples of using Storm. If this is your first time checking out Storm, check out these topologies first:

1. ExclamationTopology: Basic topology written in all Java
2. WordCountTopology: Basic topology that makes use of multilang by implementing one bolt in Python
3. ReachTopology: Example of complex DRPC on top of Storm

More information about Storm can be found on the [project page](http://github.com/nathanmarz/storm).

## Running an example with Leiningen

Install Leiningen by following the installation instructions [here](https://github.com/technomancy/leiningen). The storm-starter build uses Leiningen 1.7.1.

### To run a Java example:

```
lein deps
lein compile
java -cp $(lein classpath) storm.starter.ExclamationTopology
```

### To run a Clojure example:

```
lein deps
lein compile
lein run -m storm.starter.clj.word-count
```

## Maven

Maven is an alternative to Leiningen. storm-starter contains m2-pom.xml which can be used with Maven using the -f option. For example, to compile and run `WordCountTopology` in local mode, use this command:

```
mvn -f m2-pom.xml compile exec:java -Dexec.classpathScope=compile -Dexec.mainClass=storm.starter.WordCountTopology
```

You can package a jar suitable for submitting to a cluster with this command:

```
mvn -f m2-pom.xml package
```

This will package your code and all the non-Storm dependencies into a single "uberjar" at the path `target/storm-starter-{version}-jar-with-dependencies.jar`.
