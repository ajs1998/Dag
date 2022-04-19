# A DAG in Java

<hr>

## How to use it

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
Map<Integer, Set<Integer>> map = dag.asMap();

// Create a shallow copy
Dag<Integer> copy = dag.clone();
```
