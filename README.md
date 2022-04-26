# A `Dag` in Java

## What is it?

## What can I do with it?

### Add nodes with (parent, child) relationships to the DAG

```java
Dag<String> dag = new HashDag<>();

dag.put("Dorothy", "Shelby");
dag.put("Shelby", "Alex");
dag.put("Joe", "Alex");
```

### Add individual nodes to the DAG

```java
dag.add("Clare");
dag.add("Sarah");
```

### Find a topologically sorted list of the nodes

```java
List<String> sorted = dag.sort();
```

> `["Sarah", "Clare", "Dorothy", "Joe", "Shelby", "Alex"]`

### Find the root nodes of the DAG

```java
Set<String> roots = dag.getRoots();
```

> `["Dorothy", "Joe", "Clare", "Sarah"]`

### Find the leaf nodes of the DAG

```java
Set<String> leaves = dag.getLeaves();
```

> `["Alex", "Clare", "Sarah"]`

### Find the parents of a node

```java
Set<String> parents = dag.getParents("Alex");
```

> `["Joe", "Shelby"]`

### Find the children of a node

```java
Set<String> children = dag.getChildren("Dorothy");
```

> `["Shelby"]`

### Find the ancestors of a node

```java
Set<String> ancestors = dag.getAncestors("Alex");
```

> `["Joe", "Shelby", "Dorothy"]`

### Find the descendants of a node

```java
Set<String> descendants = dag.getDescendants("Dorothy");
```

> `["Shelby", "Alex"]`

### Get the Map representation of the DAG

```java
Map<String, Set<String>> map = dag.toMap();
```

### Create a shallow copy

```java
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
            username = {YOUR GITHUB USERNAME}
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
