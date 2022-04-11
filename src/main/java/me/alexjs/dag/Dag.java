package me.alexjs.dag;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Directed Acyclic Graph. A node may have any number of parents and children, and the graph may have any number of
 * roots and leaves.
 *
 * @param <T> The node type. It better have good {@code equals()} and {@code hashCode()} methods
 */
public interface Dag<T> {

    /**
     * Add a parent-child relationship to the DAG. If either the parent or the child are not already in the graph, each
     * will be added.
     *
     * @param parent
     * @param child
     */
    void add(T parent, T child);

    /**
     * Order the nodes of this DAG such that all of a node's children come after it in the ordering
     *
     * @return A topologically sorted list of nodes
     */
    List<T> topologicalSort();

    Set<T> getRoots();

    Set<T> getLeaves();

    Set<T> getAncestors(T node);

    Set<T> getDescendants(T node);

    Set<T> getParents(T child);

    Set<T> getChildren(T parent);

    Map<T, Set<T>> asMap();

}
