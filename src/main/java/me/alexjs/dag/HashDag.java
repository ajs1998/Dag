package me.alexjs.dag;

import java.util.*;

/**
 * An implementation of {@link Dag}.
 * The underlying structure is a {@link HashMap}.
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
     * Create a new DAG and initialize it with the contents and structure of a given {@link Map}.
     * Each key of the map will be a node, and each value will be a collection of its children.
     *
     * @param map the map to initialize this DAG with
     */
    public HashDag(Map<T, Collection<T>> map) {
        this.map = new HashMap<>();
        map.forEach(this::putAll);
    }


    /* Methods from Collection<T> */

    /**
     * Returns the number of nodes this DAG contains.
     *
     * @return the size of the DAG
     */
    @Override
    public int size() {
        return map.keySet().size();
    }

    /**
     * Returns {@literal true} if this DAG contains no nodes.
     *
     * @return {@literal true} if this DAG contains no nodes
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@literal true} if this DAG contains the specified node.
     *
     * @param node the node whose presence in this DAG is to be tested
     * @return {@literal true} if this DAG contains the specified node
     */
    @Override
    public boolean contains(Object node) {
        return map.containsKey(node);
    }

    /**
     * Returns a {@link DagIterator} over the nodes in this DAG.
     *
     * @return a {@link DagIterator} over the nodes in this DAG
     */
    @Override
    public Iterator<T> iterator() {
        // TODO
        return sort().iterator();
    }

    /**
     * Returns an array containing all the nodes in this DAG.
     *
     * @return an array containing all the nodes in this DAG
     */
    @Override
    public Object[] toArray() {
        return sort().toArray();
    }

    /**
     * Returns an array containing all the nodes in this DAG.
     *
     * @param array the array into which the nodes of this DAG are to be stored if it is big enough;
     *              otherwise, a new array of the same runtime type is allocated for this purpose.
     * @param <A>   the type of the array to create
     * @return an array containing all the nodes in this DAG
     */
    @Override
    public <A> A[] toArray(A[] array) {
        Object[] sorted = sort().toArray();
        if (array.length < sorted.length) {
            return (A[]) Arrays.copyOf(sorted, sorted.length, array.getClass());
        }
        System.arraycopy(sorted, 0, array, 0, sorted.length);
        if (array.length > sorted.length) {
            array[sorted.length] = null;
        }
        return array;
    }

    /**
     * Add a single node to this DAG.
     *
     * @param node the node to add
     * @return {@literal true} if this DAG changed as a result of the call
     */
    @Override
    public boolean add(T node) {
        return map.putIfAbsent(node, new HashSet<>()) == null;
    }

    /**
     * Remove a node from this DAG.
     * This will also remove the node from all its parents' collections of children.
     *
     * @param node the node to be removed from this DAG, if present
     * @return {@literal true} if a node was removed as a result of the call
     */
    @Override
    public boolean remove(Object node) {
        Collection<T> removed = map.remove(node);
        for (T parent : map.keySet()) {
            Collection<T> children = map.get(parent);
            if (children != null) {
                children.remove(node);
            }
        }
        return removed != null;
    }

    /**
     * Returns {@literal true} if this DAG contains all the elements in the specified collection.
     *
     * @param collection the collection to be checked for containment in this collection
     * @return {@literal true} if this DAG contains all the elements in the specified collection
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        return map.keySet().containsAll(collection);
    }

    /**
     * Add all the nodes in the specified collection to this DAG.
     *
     * @param nodes the collection containing the nodes to be added to this DAG
     */
    @Override
    public boolean addAll(Collection<? extends T> nodes) {
        boolean changed = false;
        for (T node : nodes) {
            if (add(node)) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Removes all the nodes from this DAG that are also contained in the specified collection.
     *
     * @param nodes the collection containing elements to be removed from this collection
     * @return {@literal true} if this DAG changed as a result of the call
     */
    @Override
    public boolean removeAll(Collection<?> nodes) {
        boolean changed = false;
        for (Object node : nodes) {
            if (remove(node)) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Retains only the nodes in this DAG that are also contained in the specified collection.
     *
     * @param collection the collection containing the nodes to be retained in this DAG
     * @return {@literal true} if this collection changed as a result of the call
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        // Collect the nodes to be removed
        List<T> remove = new ArrayList<>();
        for (T node : map.keySet()) {
            if (!collection.contains(node)) {
                remove.add(node);
            }
        }

        // Remove them
        boolean changed = false;
        for (T node : remove) {
            remove(node);
            changed = true;
        }

        return changed;
    }

    /**
     * Removes all the nodes from this DAG.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns {@literal true} if the specified object is equal to this DAG.
     *
     * @param o object to be compared for equality with this collection
     * @return {@literal true} if the specified object is equal to this DAG
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashDag<?> other = (HashDag<?>) o;
        return map.equals(other.map);
    }

    /**
     * Returns the hash code value for this DAG.
     *
     * @return the hash code value for this DAG
     */
    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    /**
     * Create a shallow copy of this DAG.
     * Mutations to a node in the copy will result in mutations to the node in the original DAG.
     *
     * @return a shallow copy of this DAG
     */
    @Override
    public Dag<T> clone() {
        return new HashDag<>(map);
    }


    /* Methods exclusive to Dag<T> */

    @Override
    public boolean put(T parent, T child) {
        boolean changed;
        if (!map.containsKey(parent)) {
            Set<T> children = new HashSet<>();
            children.add(child);
            changed = map.put(parent, children) != children;
        } else {
            changed = map.get(parent).add(child);
        }
        changed |= add(child);
        return changed;
    }

    @Override
    public boolean putAll(T parent, Collection<T> children) {
        boolean changed = false;
        if (!children.isEmpty()) {
            for (T child : children) {
                changed |= put(parent, child);
            }
        } else {
            changed = add(parent);
        }
        return changed;
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
                boolean hasParents = false;
                for (Collection<T> entry : copy.values()) {
                    if (entry.contains(m)) {
                        hasParents = true;
                        break;
                    }
                }
                if (!hasParents) {
                    s.add(m);
                }
            }
        }

        if (!copy.isEmpty()) {
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
    public Set<T> getNodes() {
        return toMap().keySet();
    }

    @Override
    public Map<T, Collection<T>> toMap() {
        Map<T, Collection<T>> copy = new HashMap<>();
        map.forEach((key, value) -> copy.put(key, new HashSet<>(value)));
        return copy;
    }

}
