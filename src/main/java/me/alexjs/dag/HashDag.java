package me.alexjs.dag;

import java.util.*;

/**
 * An implementation of {@link Dag}. The underlying structure is a {@link HashMap}.
 *
 * @param <T> the node type
 */
public class HashDag<T> implements Dag<T> {

    private final Map<T, Collection<T>> map;

    /**
     * Construct an empty {@link HashDag}.
     */
    public HashDag() {
        this.map = new HashMap<>();
    }

    /**
     * Construct a DAG based on a {@link Map}. Each key of the map will be a node and each node's value will be the set
     * of its children.
     *
     * @param map the
     */
    public HashDag(Map<T, Collection<T>> map) {
        this.map = new HashMap<>();
        map.forEach(this::putAll);
    }

    @Override
    public void put(T parent, T child) {
        if (!map.containsKey(parent)) {
            Set<T> children = new HashSet<>();
            children.add(child);
            map.put(parent, children);
        } else {
            map.get(parent).add(child);
        }
        add(child);
    }

    @Override
    public void putAll(T parent, Collection<T> children) {
        if (!children.isEmpty()) {
            for (T child : children) {
                put(parent, child);
            }
        } else {
            add(parent);
        }
    }

    @Override
    public void add(T node) {
        map.putIfAbsent(node, new HashSet<>());
    }

    @Override
    public void addAll(Collection<T> nodes) {
        nodes.forEach(this::add);
    }

    // Removes a node and removes it from all its parents' lists of children
    @Override
    public void remove(T node) {
        map.remove(node);
        for (T parent : map.keySet()) {
            Collection<T> children = map.get(parent);
            if (children != null) {
                children.remove(node);
            }
        }
    }

    // Removes a bunch of nodes
    @Override
    public void removeAll(Collection<T> nodes) {
        nodes.forEach(this::remove);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public List<T> sort() {

        // https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm
        // Great for running a task on these elements in a single thread

        List<T> sorted = new LinkedList<>();
        Deque<T> s = new LinkedList<>(getRoots());

        Map<T, Collection<T>> copy = toMap();

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

        if (!copy.values().stream().allMatch(Collection::isEmpty)) {
            // Circular Dependency
            return null;
        }

        return sorted;

    }

    @Override
    public Set<T> getRoots() {
        Set<T> roots = new HashSet<>(map.keySet());
        for (Collection<T> children : map.values()) {
            roots.removeAll(children);
        }
        return roots;
    }

    @Override
    public Set<T> getLeaves() {
        Set<T> leaves = new HashSet<>();
        for (Map.Entry<T, Collection<T>> entry : map.entrySet()) {
            if (entry.getValue().isEmpty()) {
                leaves.add(entry.getKey());
            }
        }
        return leaves;
    }

    @Override
    public Set<T> getParents(T node) {
        Set<T> set = new HashSet<>();
        for (Map.Entry<T, Collection<T>> entry : map.entrySet()) {
            if (entry.getValue().contains(node)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    @Override
    public Set<T> getChildren(T node) {
        Collection<T> children = map.get(node);
        if (children == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(children);
        }
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
    public Map<T, Collection<T>> toMap() {
        Map<T, Collection<T>> copy = new HashMap<>();
        map.forEach((key, value) -> copy.put(key, new HashSet<>(value)));
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashDag<?> that = (HashDag<?>) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public Dag<T> clone() {
        return new HashDag<>(map);
    }

}
