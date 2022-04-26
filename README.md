# A DAG in Java

## What is it?



## What can I do with it?

### Simple DAG operations

```java
Dag<String> dag = new HashDag<>();

// Add nodes with (parent, child) relationships to the DAG 
dag.put("Dorothy", "Shelby");
dag.put("Shelby", "Alex");
dag.put("Joe", "Alex");

// Add individual nodes to the DAG
dag.add("Clare");
dag.add("Sarah");

// Find a topologically sorted list of the nodes
// Ex: ["Sarah", "Clare", "Dorothy", "Joe", "Shelby", "Alex"]
List<String> sorted = dag.sort();

// Find the root nodes of the DAG
// Ex: ["Dorothy", "Joe", "Clare", "Sarah"]
Set<String> roots= dag.getRoots();

// Find the leaf nodes of the DAG
// Ex: ["Alex", "Clare", "Sarah"]
Set<String> leaves = dag.getLeaves();

// Find the parents of a node
// Ex: ["Joe", "Shelby"]
Set<String> parents = dag.getParents("Alex");

// Find the children of a node
// Ex: ["Shelby"]
Set<String> children = dag.getChildren("Dorothy");

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

You can use `DagUtil.traverse()` to run a task on each node in multiple threads. Each node is only visited once all of
its children have been visited. This is useful for running complex multithreaded pipelines on nodes with shared
dependencies

```java
List<Integer> result = new LinkedList<>();
DagUtil.traverse(dag, i -> result.add(i), executorService);
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
    implementation 'me.alexjs:dag:1.0.0'
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
