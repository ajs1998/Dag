package me.alexjs.dag;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link DagIterator} will return each node in a topological ordering.
 * A node is returned by {@link DagIterator#next} either if it is a leaf node with no children,
 * or after {@link DagIterator#pushParents} has already been called for all of its children nodes.
 *
 * @param <T>
 */
public class DagIterator<T> implements Iterator<T> {

    private final Dag<T> dag;
    private final AtomicBoolean hasNext;
    private final BlockingQueue<T> queue;
    private final Set<T> visited;
    private final ReentrantLock lock;

    /**
     * Create a new {@link DagIterator} for a given {@link Dag}
     * <p>
     * This iterator will return each node such that {@link DagIterator#pushParents} has already been called for all of
     * its children nodes.
     *
     * @param dag the DAG to iterate over
     */
    public DagIterator(final Dag<T> dag) {

        Dag<T> copy = dag.clone();
        Set<T> leaves = copy.getLeaves();

        // Save a copy of the dag so the original DAG is not modified
        this.dag = copy;

        // If there are no leaves (empty DAG), then we're already done iterating
        this.hasNext = new AtomicBoolean(!leaves.isEmpty());

        // Otherwise, add the leaves to the queue of nodes that are ready to be visited
        this.queue = new LinkedBlockingQueue<>(leaves);

        // Create a set of nodes representing nodes which have already been added to the queue
        this.visited = new HashSet<>(queue);

        // Makes sure that internal structures are unmodified between each call to hasNext() and next()
        this.lock = new ReentrantLock();

    }

    @Override
    public boolean hasNext() {

        // Lock internal operations
        lock.lock();

        // If hasNext is false, return it immediately, otherwise,
        // wait until an element is ready to be popped from the queue
        boolean hasNext = this.hasNext.get();
        if (hasNext) {
            while (queue.isEmpty()) ;
        }

        return hasNext;

    }

    @Override
    public T next() {

        assert hasNext.get();

        // Retrieve and remove a node off the queue
        T node = queue.poll();

        // Remove this node from the DAG we're using to keep track of nodes to be visited
        // TODO What to do with null nodes in the DAG?
        //  I think there's no reason they couldn't be supported
        dag.remove(node);

        // If the DAG is empty, then there are no more nodes to visit
        hasNext.set(!dag.isEmpty());

        // Unlock internal operations
        lock.unlock();

        return node;

    }

    public void pushParents(T child) {

        // Lock internal operations
        lock.lock();

        // Add the node's parents to the queue
        Set<T> parents = dag.getParents(child);
        for (T parent : parents) {
            // If the node hasn't already been visited, then add it to the queue and the visited set
            if (visited.add(parent)) {
                queue.add(parent);
            }
        }

        // Unlock internal operations
        lock.unlock();

    }

}
