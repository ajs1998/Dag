package me.alexjs.dag;

import java.util.*;

public class SimpleForest<T> implements Dag<T> {

    private final Map<T, Set<T>> forest;

    public SimpleForest() {
        this.forest = new HashMap<>();
    }

    public SimpleForest(Map<T, Set<T>> forest) {
        this.forest = new HashMap<>();
        for (Map.Entry<T, Set<T>> entry : forest.entrySet()) {
            if (entry.getValue().isEmpty()) {
                add(entry.getKey());
            } else {
                for (T child : entry.getValue()) {
                    put(entry.getKey(), child);
                }
            }
        }
    }

    @Override
    public void put(T parent, T child) {
        if (child == null) {
            add(parent);
        } else {
            if (!forest.containsKey(parent)) {
                Set<T> children = new HashSet<>();
                children.add(child);
                forest.put(parent, children);
            } else {
                forest.get(parent).add(child);
            }
            add(child);
        }
    }

    @Override
    public void add(T node) {
        forest.putIfAbsent(node, new HashSet<>());
    }

    @Override
    public List<T> topologicalSort() {

        // https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm
        // Great for running a task on these elements in a single thread

        List<T> sorted = new LinkedList<>();
        Deque<T> s = new LinkedList<>(getRoots());

        Map<T, Set<T>> copy = asMap();

        while (!s.isEmpty()) {
            T n = s.pop();
            sorted.add(n);
            for (T m : copy.remove(n)) {
                boolean hasParents = copy.values().stream().anyMatch(entry -> entry.contains(m));
                if (!hasParents) {
                    s.add(m);
                }
            }
        }

        if (!copy.values().stream().allMatch(Set::isEmpty)) {
            // Circular Dependency
            return null;
        }

        return sorted;

    }

    @Override
    public Set<T> getRoots() {
        Set<T> roots = new HashSet<>(forest.keySet());
        for (Set<T> children : forest.values()) {
            roots.removeAll(children);
        }
        return roots;
    }

    @Override
    public Set<T> getLeaves() {
        Set<T> leaves = new HashSet<>();
        for (Map.Entry<T, Set<T>> entry : forest.entrySet()) {
            if (entry.getValue().isEmpty()) {
                leaves.add(entry.getKey());
            }
        }
        return leaves;
    }

    @Override
    public Set<T> getAncestors(T node) {
        Set<T> ancestors = getParents(node);
        for (T ancestor : new HashSet<>(ancestors)) {
            ancestors.addAll(getAncestors(ancestor));
        }
        return ancestors;
    }

    @Override
    public Set<T> getDescendants(T node) {
        Set<T> descendants = getChildren(node);
        for (T descendant : new HashSet<>(descendants)) {
            descendants.addAll(getDescendants(descendant));
        }
        return descendants;
    }

    @Override
    public Set<T> getParents(T child) {
        Set<T> set = new HashSet<>();
        for (Map.Entry<T, Set<T>> entry : forest.entrySet()) {
            if (entry.getValue().contains(child)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    @Override
    public Set<T> getChildren(T parent) {
        Set<T> children = forest.get(parent);
        if (children == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(children);
        }
    }

    @Override
    public Map<T, Set<T>> asMap() {
        Map<T, Set<T>> copy = new HashMap<>();
        forest.forEach((key, value) -> copy.put(key, new HashSet<>(value)));
        return copy;
    }

}
