package me.alexjs.dag;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Directed Acyclic Graph with the spirit of something the Java Collections Framework
 *
 * @param <E> The node type
 */
public interface Dag<E> extends Collection<E>, Cloneable, Serializable {

    /**
     * Adds a {@code source} to {@code target} node relationship to this DAG.
     * If either node is not already in the graph, then it will be added.
     *
     * @param source the source node
     * @param target the target node
     * @return {@code true} if this DAG changed as a result of the call
     */
    boolean put(E source, E target);

    /**
     * Adds many {@code source} to {@code target} node relationships to this DAG.
     * If the source node or any of its target nodes are not already in the graph, then they will be added.
     *
     * @param source  the source node
     * @param targets the target nodes
     * @return {@code true} if this DAG changed as a result of the call
     */
    boolean putAll(E source, Collection<E> targets);

    /**
     * Removes an edge from this DAG.
     * Only the edge will be removed, not the given nodes.
     *
     * @param source the source node
     * @param target the target node
     * @return {@code true} if this DAG changed as a result of the call
     */
    boolean removeEdge(E source, E target);

    /**
     * Orders the nodes of this DAG such that each node comes before its outgoing nodes in the ordering
     *
     * @return a list of nodes in topological order, or {@code null} if there's a circular dependency
     * @see <a href="https://en.wikipedia.org/wiki/Topological_sorting">https://en.wikipedia.org/wiki/Topological_sorting</a>
     */
    List<E> sort();

    /**
     * Gets the nodes of this DAG that have no incoming edges
     *
     * @return the root nodes
     */
    Set<E> getRoots();

    /**
     * Gets the nodes of this DAG that have no outgoing edges
     *
     * @return the leaf nodes
     */
    Set<E> getLeaves();

    /**
     * Gets the nodes of the given node's incoming edges
     *
     * @param node the node
     * @return the incoming nodes of the given node
     */
    Set<E> getIncoming(E node);

    /**
     * Gets the nodes of the given node's outgoing edges
     *
     * @param node the node
     * @return the outgoing nodes of the given node
     */
    Set<E> getOutgoing(E node);

    /**
     * Gets the set of nodes such that each can reach the given node
     *
     * @param node the node
     * @return the ancestor nodes of the given node
     */
    Set<E> getAncestors(E node);

    /**
     * Gets the set of nodes such that each is reachable from the given node
     *
     * @param node the node
     * @return the descendant nodes of the given node
     */
    Set<E> getDescendants(E node);

    /**
     * Gets the full set of nodes this DAG contains
     *
     * @return the nodes this DAG contains
     */
    Set<E> getNodes();

    /**
     * Creates a DAG with the directions of all edges flipped.
     * The ancestors of a node become descendants and vice-versa.
     *
     * @return a DAG with the directions of all edges flipped
     */
    Dag<E> inverted();

    /**
     * Creates a {@link Map} representation of this DAG.
     * Each key of the map will be a node, and each value is a collection of that node's outgoing nodes.
     *
     * @return a map representation of this DAG
     */
    Map<E, Collection<E>> toMap();

    /**
     * Creates a shallow copy of this DAG.
     * Use this with caution if {@link E} is mutable.
     *
     * @return a shallow copy of this DAG
     */
    Dag<E> clone();

}
