package me.alexjs.dag;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Directed Acyclic Graph
 *
 * @param <T> The node type
 */
public interface Dag<T> extends Collection<T>, Cloneable, Serializable {

    /**
     * Adds a {@code source} -> {@code target} node relationship to this DAG.
     * If either node is not already in the graph, then it will be added.
     *
     * @param source the source node
     * @param target the target node
     * @return {@code true} if this DAG changed as a result of the call
     */
    boolean put(T source, T target);

    /**
     * Adds many {@code source} -> {@code target} node relationships to this DAG.
     * If the source node or any of its target nodes are not already in the graph, then they will be added.
     *
     * @param source  the source node
     * @param targets the target nodes
     * @return {@code true} if this DAG changed as a result of the call
     */
    boolean putAll(T source, Collection<T> targets);

    /**
     * Removes an edge from this DAG.
     * Only the edge will be removed, not the given nodes.
     *
     * @param source the source node
     * @param target the target node
     * @return {@code true} if this DAG changed as a result of the call
     */
    boolean removeEdge(T source, T target);

    /**
     * Orders the nodes of this DAG such that each node comes before its outgoing nodes in the ordering
     *
     * @return a list of nodes in topological order, or {@code null} if there's a circular dependency
     * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">https://en.wikipedia.org/wiki/Topological_sorting</a>
     */
    List<T> sort();

    /**
     * Gets the nodes of this DAG that have no incoming edges
     *
     * @return the root nodes
     */
    Set<T> getRoots();

    /**
     * Gets the nodes of this DAG that have no outgoing edges
     *
     * @return the leaf nodes
     */
    Set<T> getLeaves();

    /**
     * Gets the nodes of the given node's incoming edges
     *
     * @param node the node
     * @return the incoming nodes of the given node
     */
    Set<T> getIncoming(T node);

    /**
     * Gets the nodes of the given node's outgoing edges
     *
     * @param node the node
     * @return the outgoing nodes of the given node
     */
    Set<T> getOutgoing(T node);

    /**
     * Gets the set of nodes such that each can reach the given node
     *
     * @param node the node
     * @return the ancestor nodes of the given node
     */
    Set<T> getAncestors(T node);

    /**
     * Gets the set of nodes such that each is reachable from the given node
     *
     * @param node the node
     * @return the descendant nodes of the given node
     */
    Set<T> getDescendants(T node);

    /**
     * Gets the full set of nodes this DAG contains
     *
     * @return the nodes this DAG contains
     */
    Set<T> getNodes();

    /**
     * Creates a {@link Map} representation of this DAG.
     * Each key of the map will be a node, and each value is a collection of that node's outgoing nodes.
     *
     * @return a map representation of this DAG
     */
    Map<T, Collection<T>> toMap();

    /**
     * Creates a shallow copy of this DAG.
     * Use this with caution if {@link T} is mutable.
     *
     * @return a shallow copy of this DAG
     */
    Dag<T> clone();

}
