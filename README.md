# A DAG in Java

## What is it?

It's a simple library for creating an interacting with a directed acyclic graph structure in Java.
It includes a `Dag<T>` interface so you can provide your own implementation.
`HashDag<T>` is a helpful implementation of `Dag<T>` where the underlying structure is `HashMap<T, HashSet<T>>`.

`Dag<T>` also extends `Collection<T>` so you can use it as you would anything else in the Java Collections API.

## What can I do with it?

### Simple DAG operations

```java
Dag<String> dag = new HashDag<>();

// Add nodes with source -> target relationships to the DAG 
dag.put("Dorothy", "Shelby");
dag.put("Shelby", "Alex");
dag.put("Joe", "Alex");

// Add individual nodes to the DAG
dag.add("Clare");
dag.add("Sarah");

// Find a topologically sorted list of the nodes
// Ex: ["Alex", "Joe", "Sarah", "Shelby", "Dorothy", "Clare"]
// Ex: ["Dorothy", "Shelby", "Clare", "Joe", "Alex", "Sarah"]
List<String> sorted = dag.sort();

// Find the root nodes of the DAG
// Ex: ["Dorothy", "Joe", "Clare", "Sarah"]
Set<String> roots = dag.getRoots();

// Find the leaf nodes of the DAG
// Ex: ["Alex", "Clare", "Sarah"]
Set<String> leaves = dag.getLeaves();

// Find a node's incoming nodes
// Ex: ["Joe", "Shelby"]
Set<String> incoming = dag.getIncoming("Alex");

// Find a node's outgoing nodes
// Ex: ["Shelby"]
Set<String> outgoing = dag.getOutgoing("Dorothy");

// Find the ancestors of a node
// Ex: ["Joe", "Shelby", "Dorothy"]
Set<String> ancestors = dag.getAncestors("Alex");

// Find the descendants of a node
// Ex: ["Shelby", "Alex"]
Set<String> descendants = dag.getDescendants("Dorothy");

// Get the Map representation of the DAG
Map<String, Set<String>> map = dag.toMap();

// Create a shallow copy
Dag<String> copy = dag.clone();
```

### DAG Traversal

You can use a `DagTraversalTask` to run a task on each node in multiple threads. Each node is only visited once all of
its incoming nodes have been visited. This is useful for running complex multithreaded pipelines on nodes with shared
dependencies.

```java
ExecutorService executorService = Executors.newFixedThreadPool(3);

List<Integer> result = new LinkedList<>();
DagTraversalTask<?> traverser = new DagTraversalTask<>(dag, result::add, executorService);
boolean success = traverser.awaitTermination(10, TimeUnit.MINUTES));
```

## How do I get it?

### Gradle package from GitHub Packages

```gradle
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/ajs1998/Dag')
        credentials {
            username = {YOUR GITHUB PAT}
            // This is a PAT (Personal Access Token) that only has permission to read/download public GitHub Packages.
            // This is not the actual password for the account.
            password = {YOUR GITHUB PAT}
        }
    }
}
```

```gradle
dependencies {
    implementation 'me.alexjs:dag:2.0.0'
}
```

You need to <a href="https://github.com/settings/tokens">create a GitHub Personal Access Token (PAT)</a> to be able to
download GitHub Packages. The token only needs the `read:packages` permission to work.

### Maven Central package

Coming soon

## Notes

- Flexible `Dag<T>` interface so you can write your own implementation
- 100% test coverage
- 100% Javadoc coverage

[![CI](https://github.com/ajs1998/Dag/actions/workflows/test.yml/badge.svg)](https://github.com/ajs1998/Dag/actions/workflows/test.yml)
