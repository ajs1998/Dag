package me.alexjs.dag;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Directed Acyclic Graph.
 *
 * @param <T> The node type
 */
public interface Dag<T> extends Collection<T>, Cloneable, Serializable {

    /**
     * Add a parent-child relationship to this DAG.
     * If either the parent or the child are not already in the graph, each will be added.
     *
     * @param parent the parent node
     * @param child  the parent node's child
     * @return
     */
    boolean put(T parent, T child);

    /**
     * Add many parent-child relationships to this DAG.
     * If the parent or any of its children are not already in the graph, each will be added.
     *
     * @param parent   the parent
     * @param children the parent node's children
     * @return
     */
    boolean putAll(T parent, Collection<T> children);

    /**
     * Order the nodes of this DAG such that all of a node's children come after it in the ordering.
     *
     * @return a topologically sorted list of nodes, or {@code null} if there's a circular dependency
     * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">https://en.wikipedia.org/wiki/Topological_sorting</a>
     */
    List<T> sort();

    /**
     * Get the nodes of this DAG that have no parents.
     *
     * @return the root nodes
     */
    Set<T> getRoots();

    /**
     * Get the nodes of this DAG that have no children.
     *
     * @return the leaf nodes
     */
    Set<T> getLeaves();

    /**
     * Get the parents of a given node.
     *
     * @param node the node
     * @return the parent nodes of the given node
     */
    Set<T> getParents(T node);

    /**
     * Get the children of a given node.
     *
     * @param node the node
     * @return the child nodes of the given node
     */
    Set<T> getChildren(T node);

    /**
     * Get the ancestor nodes of a given node.
     *
     * @param node the node
     * @return the ancestor nodes of the given node
     */
    Set<T> getAncestors(T node);

    /**
     * Get the descendant nodes of a given node.
     *
     * @param node the node
     * @return the descendant nodes of the given node
     */
    Set<T> getDescendants(T node);

    /**
     * Get the full set of nodes this DAG contains.
     *
     * @return the nodes this DAG contains
     */
    Set<T> getNodes();

    /**
     * Create a {@link Map} representation of this DAG.
     * Each node will be a key of the map, and each value is a collection of a node's children.
     *
     * @return a map representation of this DAG
     */
    Map<T, Collection<T>> toMap();

    /**
     * Create a shallow copy of this DAG. Mutations to a node in the copy will result in mutations to the node in the
     * original DAG.
     *
     * @return a shallow copy of this DAG
     */
    Dag<T> clone();

}
