package me.alexjs.dag;

import java.util.*;

/**
 * An implementation of {@link Dag} where the underlying structure is a {@link HashMap}
 * <p>
 * This implementation was created with the intent to be used for traversing dependencies.
 * For this reason, (parent, child) relationships are reversed compared to how they're described on Wikipedia.
 * It's easier to determine "A depends on B[]" than "B is connected to A[]".
 * The {@link HashDag#sort()} and {@link HashDag#iterator()} methods order the nodes such that each is visited only
 * after all its children (dependents) have already been visited.
 *
 * @param <E> the node type
 */
public class HashDag<E> implements Dag<E> {

    /**
     * The backing {@link Map} that represents this DAG.
     * Each node is a key in this map, its values are the nodes it "depends" on.
     */
    private final Map<E, Collection<E>> map;

    /**
     * Construct an empty {@link HashDag}
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
    public HashDag(Map<E, Collection<E>> map) {
        this.map = new HashMap<>();
        map.forEach(this::putAll);
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
     * @param node the node whose presence in this DAG is to be tested
     * @return {@code true} if this DAG contains the specified node
     */
    @Override
    public boolean contains(Object node) {
        return map.containsKey(node);
    }

    /**
     * Returns a {@link Iterator} over the nodes in this DAG.
     * The iterator will return nodes in reverse-topological order.
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
     * @param array the array into which the nodes of this DAG are to be stored if it is big enough;
     *              otherwise, a new array of the same runtime type is allocated for this purpose.
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
     * Add a single node to this DAG
     *
     * @param node the node to add
     * @return {@code true} if this DAG changed as a result of the call
     */
    @Override
    public boolean add(E node) {
        return map.putIfAbsent(node, new HashSet<>()) == null;
    }

    /**
     * Remove a node from this DAG.
     * This will also remove the node from all its parents' collections of children.
     *
     * @param node the node to be removed from this DAG, if present
     * @return {@code true} if a node was removed as a result of the call
     */
    @Override
    public boolean remove(Object node) {
        boolean removed = map.remove(node) != null;
        for (E parent : map.keySet()) {
            Collection<E> children = map.get(parent);
            if (children != null) {
                removed |= children.remove(node);
            }
        }
        return removed;
    }

    /**
     * Returns {@code true} if this DAG contains all the elements in the specified collection
     *
     * @param collection the collection to be checked for containment in this collection
     * @return {@code true} if this DAG contains all the elements in the specified collection
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
    public boolean addAll(Collection<? extends E> nodes) {
        boolean changed = false;
        for (E node : nodes) {
            if (add(node)) {
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Removes all the nodes from this DAG that are also contained in the specified collection
     *
     * @param nodes the collection containing elements to be removed from this collection
     * @return {@code true} if this DAG changed as a result of the call
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
     * Retains only the nodes in this DAG that are also contained in the specified collection
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
     * Returns {@code true} if the specified object is equal to this DAG
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
     * Create a shallow copy of this DAG.
     * Mutations to a node in the copy will result in mutations to the node in the original DAG.
     *
     * @return a shallow copy of this DAG
     */
    @Override
    public Dag<E> clone() {
        return new HashDag<>(map);
    }


    /* Methods exclusive to Dag<T> */

    @Override
    public boolean put(E parent, E child) {
        boolean changed;
        if (!map.containsKey(parent)) {
            Set<E> children = new HashSet<>();
            children.add(child);
            changed = map.put(parent, children) != children;
        } else {
            changed = map.get(parent).add(child);
        }
        changed |= add(child);
        return changed;
    }

    @Override
    public boolean putAll(E parent, Collection<E> children) {
        boolean changed = false;
        if (!children.isEmpty()) {
            for (E child : children) {
                changed |= put(parent, child);
            }
        } else {
            changed = add(parent);
        }
        return changed;
    }

    @Override
    public List<E> sort() {

        // https://en.wikipedia.org/wiki/Topological_sorting#Kahn's_algorithm
        // Great for running a task on these elements in a single thread

        List<E> sorted = new LinkedList<>();
        Deque<E> s = new LinkedList<>(getRoots());

        Map<E, Collection<E>> copy = toMap();

        while (!s.isEmpty()) {
            E n = s.pop();
            sorted.add(n);
            for (E m : copy.remove(n)) {
                boolean hasParents = false;
                for (Collection<E> entry : copy.values()) {
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

        // Reverse it because reverse-topological order is more convenient
        Collections.reverse(sorted);
        return sorted;

    }

    @Override
    public Set<E> getRoots() {
        Set<E> roots = new HashSet<>(map.keySet());
        for (Collection<E> children : map.values()) {
            roots.removeAll(children);
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
    public Set<E> getParents(E node) {
        Set<E> set = new HashSet<>();
        for (Map.Entry<E, Collection<E>> entry : map.entrySet()) {
            if (entry.getValue().contains(node)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    @Override
    public Set<E> getChildren(E node) {
        Collection<E> children = map.get(node);
        if (children == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>(children);
        }
    }

    @Override
    public Set<E> getAncestors(E node) {
        Set<E> ancestors = getParents(node);
        for (E ancestor : new HashSet<>(ancestors)) {
            ancestors.addAll(getAncestors(ancestor));
        }
        return ancestors;
    }

    @Override
    public Set<E> getDescendants(E node) {
        Set<E> descendants = getChildren(node);
        for (E descendant : new HashSet<>(descendants)) {
            descendants.addAll(getDescendants(descendant));
        }
        return descendants;
    }

    @Override
    public Set<E> getNodes() {
        return toMap().keySet();
    }

    @Override
    public Map<E, Collection<E>> toMap() {
        Map<E, Collection<E>> copy = new HashMap<>();
        map.forEach((key, value) -> copy.put(key, new HashSet<>(value)));
        return copy;
    }

}
