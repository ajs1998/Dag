package me.alexjs.dag;

import java.util.*;

/**
 * An implementation of {@link Dag} where the underlying structure is a {@link HashMap}
 * <p>
 * This implementation was created with the intent to be used for traversing dependencies.
 * The {@link HashDag#sort()} and {@link HashDag#iterator()} methods order the nodes such that each is visited only
 * after all its ancestor nodes have already been visited.
 *
 * @param <E> the node type
 */
public class HashDag<E> implements Dag<E> {

    /**
     * The backing {@link Map} that represents this DAG.
     * Each key of the map is a node, and each value is a collection of that node's outgoing nodes.
     */
    private final Map<E, Collection<E>> map;

    /**
     * Constructs an empty {@link HashDag}
     */
    public HashDag() {
        this.map = new HashMap<>();
    }

    /**
     * Creates a new DAG and initialize it with the contents and structure of a given {@link Map}.
     * Each key of the map is a node, and each value is a collection of that node's outgoing nodes.
     *
     * @param map the map to initialize this DAG with
     */
    public HashDag(Map<E, Collection<E>> map) {
        this.map = new HashMap<>();
        map.forEach(this::putAll);
    }


    /* Methods exclusive to Dag<> */

    @Override
    public boolean put(E source, E target) {
        boolean changed;
        if (!map.containsKey(source)) {
            Set<E> targets = new HashSet<>();
            targets.add(target);
            changed = map.put(source, targets) != targets;
        } else {
            changed = map.get(source).add(target);
        }
        changed |= add(target);
        return changed;
    }

    @Override
    public boolean putAll(E source, Collection<E> targets) {
        boolean changed = false;
        if (!targets.isEmpty()) {
            for (E target : targets) {
                changed |= put(source, target);
            }
        } else {
            changed = add(source);
        }
        return changed;
    }

    @Override
    public boolean removeEdge(E source, E target) {
        return map.containsKey(source) && map.get(source).remove(target);
    }

    @Override
    public List<E> sort() {

        // https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm
        // Great for running a task on these elements in a single thread

        List<E> sorted = new LinkedList<>();
        Deque<E> s = new LinkedList<>(getRoots());

        Map<E, Collection<E>> copy = this.toMap();

        while (!s.isEmpty()) {
            E n = s.pop();
            sorted.add(n);

            for (E m : copy.remove(n)) {
                boolean hasIncoming = false;
                for (Collection<E> entry : copy.values()) {
                    if (entry.contains(m)) {
                        hasIncoming = true;
                        break;
                    }
                }
                if (!hasIncoming) {
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
    public Set<E> getRoots() {
        Set<E> roots = new HashSet<>(map.keySet());
        for (Collection<E> targets : map.values()) {
            roots.removeAll(targets);
        }
        return roots;
    }

    @Override
    public Set<E> getLeaves() {
        Set<E> leaves = new HashSet<>();
        for (Map.Entry<E, Collection<E>> entry : map.entrySet()) {
            if (entry.getValue().isEmpty()) {
                leaves.add(entry.getKey());
            }
        }
        return leaves;
    }

    @Override
    public Set<E> getIncoming(E node) {
        Set<E> set = new HashSet<>();
        for (Map.Entry<E, Collection<E>> entry : map.entrySet()) {
            if (entry.getValue().contains(node)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    @Override
    public Set<E> getOutgoing(E node) {
        Collection<E> outgoing = map.get(node);
        if (outgoing == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(outgoing);
        }
    }

    @Override
    public Set<E> getAncestors(E node) {
        checkForCircularDependency();
        return getAncestorsImpl(node);
    }

    private Set<E> getAncestorsImpl(E node) {
        Set<E> ancestors = getIncoming(node);
        for (E ancestor : new HashSet<>(ancestors)) {
            ancestors.addAll(getAncestorsImpl(ancestor));
        }
        return ancestors;
    }

    @Override
    public Set<E> getDescendants(E node) {
        checkForCircularDependency();
        return getDescendantsImpl(node);
    }

    private Set<E> getDescendantsImpl(E node) {
        Set<E> descendants = getOutgoing(node);
        for (E descendant : new HashSet<>(descendants)) {
            descendants.addAll(getDescendantsImpl(descendant));
        }
        return descendants;
    }

    @Override
    public Set<E> getNodes() {
        return toMap().keySet();
    }

    @Override
    public Dag<E> inverted() {
        Map<E, Collection<E>> result = new HashMap<>();
        this.map.forEach((source, targets) -> {
            for (E target : targets) {
                if (!result.containsKey(target)) {
                    result.put(target, new HashSet<>());
                }
                result.get(target).add(source);
            }
        });
        return new HashDag<>(result);
    }

    @Override
    public Map<E, Collection<E>> toMap() {
        Map<E, Collection<E>> copy = new HashMap<>();
        map.forEach((key, value) -> copy.put(key, new HashSet<>(value)));
        return copy;
    }


    /* Methods from Collection<T> */

    /**
     * Returns the number of nodes this DAG contains
     *
     * @return the size of the DAG
     */
    @Override
    public int size() {
        return map.keySet().size();
    }

    /**
     * Returns {@code true} if this DAG contains no nodes
     *
     * @return {@code true} if this DAG contains no nodes
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@code true} if this DAG contains the specified node
     *
     * @param node the node whose presence is to be tested
     * @return {@code true} if this DAG contains the specified node
     */
    @Override
    public boolean contains(Object node) {
        return map.containsKey(node);
    }

    /**
     * Returns a {@link Iterator} over the nodes in this DAG.
     * The iterator will return nodes in topological order.
     *
     * @return a {@link Iterator} over the nodes in this DAG
     */
    @Override
    public Iterator<E> iterator() {
        return sort().iterator();
    }

    /**
     * Returns an array containing all the nodes in this DAG
     *
     * @return an array containing all the nodes in this DAG
     */
    @Override
    public Object[] toArray() {
        return sort().toArray();
    }


    /**
     * Returns an array containing all the nodes in this DAG
     *
     * @param array the array into which the nodes of this DAG will be stored if it is big enough.
     *              Otherwise, a new array of the same runtime type is allocated for this purpose.
     * @param <T>   the type of the array to create
     * @return an array containing all the nodes in this DAG
     */
    @Override
    public <T> T[] toArray(T[] array) {
        @SuppressWarnings("unchecked")
        T[] sorted = (T[]) sort().toArray();
        if (array.length < sorted.length) {
            @SuppressWarnings("unchecked")
            T[] result = (T[]) Arrays.copyOf(sorted, sorted.length, array.getClass());
            return result;
        }
        System.arraycopy(sorted, 0, array, 0, sorted.length);
        if (array.length > sorted.length) {
            array[sorted.length] = null;
        }
        return array;
    }

    /**
     * Adds a single node to this DAG
     *
     * @param node the node to add
     * @return {@code true} if this DAG changed as a result of the call
     */
    @Override
    public boolean add(E node) {
        return map.putIfAbsent(node, new HashSet<>()) == null;
    }

    /**
     * Removes a node and all its incoming and outgoing edges from this DAG.
     *
     * @param node the node to be removed from this DAG, if present
     * @return {@code true} if the node was removed as a result of the call
     */
    @Override
    public boolean remove(Object node) {
        boolean removed = map.remove(node) != null;
        for (E source : map.keySet()) {
            Collection<E> outgoing = map.get(source);
            if (outgoing != null) {
                removed |= outgoing.remove(node);
            }
        }
        return removed;
    }

    /**
     * Returns {@code true} if this DAG contains all the elements in the specified collection
     *
     * @param collection the collection of nodes whose entire presence is to be tested
     * @return {@code true} if this DAG contains all the elements in the specified collection
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        return map.keySet().containsAll(collection);
    }

    /**
     * Adds each node in the specified collection to this DAG if it is not already present
     *
     * @param nodes the collection of nodes to be added to this DAG
     * @return {@code true} if this DAG changed as a result of the call
     */
    @Override
    public boolean addAll(Collection<? extends E> nodes) {
        boolean changed = false;
        for (E node : nodes) {
            changed |= add(node);
        }
        return changed;
    }

    /**
     * Removes each node in the specified collection and all their incoming and outgoing edges from this DAG
     *
     * @param nodes the collection of nodes to be removed from this DAG, if present
     * @return {@code true} if this DAG changed as a result of the call
     */
    @Override
    public boolean removeAll(Collection<?> nodes) {
        boolean changed = false;
        for (Object node : nodes) {
            changed |= remove(node);
        }
        return changed;
    }

    /**
     * Retains only the nodes in this DAG that are also present in the specified collection
     *
     * @param collection the collection containing the nodes to be retained in this DAG
     * @return {@code true} if this collection changed as a result of the call
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        // Collect the nodes to be removed
        List<E> remove = new ArrayList<>();
        for (E node : map.keySet()) {
            if (!collection.contains(node)) {
                remove.add(node);
            }
        }

        // Remove them
        boolean changed = false;
        for (E node : remove) {
            remove(node);
            changed = true;
        }

        return changed;
    }

    /**
     * Removes all the nodes from this DAG
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Compares the specified object with this DAG for equality
     *
     * @param o object to be compared for equality with this collection
     * @return {@code true} if the specified object is equal to this DAG
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashDag<?> other = (HashDag<?>) o;
        return map.equals(other.map);
    }

    /**
     * Returns the hash code value for this DAG
     *
     * @return the hash code value for this DAG
     */
    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    /**
     * Creates a shallow copy of this DAG.
     * Mutations to a node in the copy will result in mutations to the node in the original DAG.
     *
     * @return a shallow copy of this DAG
     */
    @Override
    public Dag<E> clone() {
        return new HashDag<>(map);
    }

    private void checkForCircularDependency() {
        if (sort() == null) {
            throw new IllegalArgumentException("DAG contains a circular dependency");
        }
    }

}
