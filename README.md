# A DAG in Java

## How do I get it?

### Gradle package from GitHub Packages

```gradle
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/ajs1998/Dag')
        credentials {
            username = 'ajs1998'
            // This is a PAT (Personal Access Token) that only has permission to read/download public GitHub Packages.
            // This is not the actual password for the account.
            password = {YOUR GITHUB PAT}
        }
    }
}
```

```gradle
dependencies {
    implementation 'me.alexjs:dag:1.9'
}
```

You need to <a href="https://github.com/settings/tokens">create a GitHub Personal Access Token (PAT)</a> to be able to
download GitHub Packages. The token only needs the `read:packages` permission to work.

### Maven Central package

Coming soon

## What can I do with it?

```java
Dag<Integer> dag = new HashDag<>();

// Add nodes with (parent, child) relationships to the DAG 
dag.put(0, 1);
dag.put(2, 3);

// Add individual nodes to the DAG
dag.add(4);
dag.add(5);

// Find a topologically sorted list of the nodes
// Ex: [4, 5, 0, 2, 1, 3]
List<Integer> sorted = dag.sort();

// Find the root nodes of the DAG
// Ex: [0, 2, 4, 5]
Set<Integer> roots= dag.getRoots();

// Find the leaf nodes of the DAG
// Ex: [1, 3, 4, 5]
Set<Integer> leaves = dag.getLeaves();

// Find the parents of a node
// Ex: [0]
Set<Integer> parents = dag.getParents(1);

// Find the children of a node
// Ex: [1]
Set<Integer> children = dag.getChildren(0);

// Find the ancestors of a node
// Ex: [2]
Set<Integer> ancestors = dag.getAncestors(3);

// Find the descendants of a node
// Ex: [3]
Set<Integer> descendants = dag.getDescendants(2);

// Get the Map representation of the DAG
Map<Integer, Set<Integer>> map = dag.toMap();

// Create a shallow copy
Dag<Integer> copy = dag.clone();
```

### DAG Traversal

You can use `DagUtil.traverse()` to run a task on each node in multiple threads. Each node is only visited once all of
its children have been visited. This is useful for running complex multithreaded pipelines on nodes with shared
dependencies

```java
List<Integer> result = new LinkedList<>();
DagUtil.traverse(dag, i -> result.add(i), executorService);
```

## Notes

- Flexible `Dag<T>` interface so you can write your own implementation
- 100% test coverage
- 100% Javadoc coverage
