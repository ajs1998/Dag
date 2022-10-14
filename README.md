# A DAG in Java

## What is it?

It's a simple library for creating an interacting with a directed acyclic graph structure in Java.
It includes a `Dag<T>` interface so you can provide your own implementation.
`HashDag<T>` is a helpful implementation where the underlying structure is `HashMap<T, HashSet<T>>`.

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
// Ex: ["Dorothy", "Joe", "Sarah", "Clare", "Shelby", "Alex"]
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

// Invert the graph edges
Dag<String> inverted = dag.inverted();
```

### DAG Traversal

You can use a `DagTraversalTask` to run a task on each node in multiple threads. Each node is only visited once all of
its incoming nodes have been visited. This is useful for running complex multithreaded pipelines on nodes with shared
dependencies.

```java
ExecutorService executorService = Executors.newFixedThreadPool(3);

List<Integer> result = Collections.synchronizedList(new LinkedList<>());
DagTraversalTask<?> task = new DagTraversalTask<>(dag, result::add, executorService);
boolean success = task.awaitTermination(10, TimeUnit.MINUTES));
```

## How do I get it?

[Maven Central package](https://search.maven.org/artifact/me.alexjs/dag)

```groovy
implementation 'me.alexjs:dag:2.1.0'
```

## Notes

- Flexible `Dag<T>` interface so you can write your own implementation
- 100% test coverage
- 100% Javadoc coverage

## Contribute
Please make a PR or an Issue for any question, bug, feature, or any other request.
I'm happy to keep this repository well-maintained.

[![CI](https://github.com/ajs1998/Dag/actions/workflows/test.yml/badge.svg)](https://github.com/ajs1998/Dag/actions/workflows/test.yml)
