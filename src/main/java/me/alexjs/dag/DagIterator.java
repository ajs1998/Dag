package me.alexjs.dag;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link DagIterator} will return each node in a topological ordering.
 * A node is returned by {@link DagIterator#next} either if it is a leaf node with no children,
 * or after {@link DagIterator#pushParents} has already been called for all of its children nodes.
 *
 * @param <T> the type of the nodes that are returned by this iterator
 */
public class DagIterator<T> implements Iterator<T> {

    private final Dag<T> dag;
    private final BlockingQueue<T> queue;
    private final Set<T> queued;
    private final ReentrantReadWriteLock lock;

    private boolean hasNext;
    private boolean waiting = false;

    /**
     * Create a new {@link DagIterator} for a given {@link Dag}.
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

        // Otherwise, add the leaves to the queue of nodes that are ready to be visited
        this.queue = new LinkedBlockingQueue<>(leaves);

        // Create a set of nodes representing nodes which have already been added to the queue
        this.queued = new HashSet<>(leaves);

        // Makes sure that internal structures are unmodified between each call to hasNext() and next()
        this.lock = new ReentrantReadWriteLock(true);

        // If there are nodes in the queue, then we're already done iterating
        this.hasNext = !leaves.isEmpty();

    }

    @Override
    public boolean hasNext() {

        try {
            lock.writeLock().lock();
            waiting = true;
            return hasNext;
        }finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public T next() {

        try {

            // Retrieve and remove a node off the queue
            return queue.take();

        } catch (InterruptedException e) {

            // TODO clear the queue, clear the DAG, hasNext = false
            throw new CompletionException(e);

        } finally {
            waiting = false;
        }

    }

    /**
     * Push the parents of a given {@code node} to this iterator.
     * This is used to ensure nodes are only visited once all their children have already been visited.
     *
     * @param node the node whose parents need to be enqueued
     */
    public void pushParents(T node) {

        // Get the node's parents before we remove the node from the DAG
        Set<T> enqueue = dag.getParents(node);

        // Remove this node from the DAG we're using to keep track of nodes to be enqueued
        dag.remove(node);

        lock.writeLock().lock();
        if (waiting) {
            if (dag.isEmpty()) {
                hasNext = false;
            }

            // Don't enqueue nodes that have already been enqueued
            enqueue.removeAll(queued);

            // Don't enqueue parent nodes that still have children
            enqueue.removeIf(p -> !dag.getChildren(p).isEmpty());

            // Add the nodes to the queue
            queue.addAll(enqueue);

            // Continue to keep track of which nodes we have enqueued
            queued.addAll(enqueue);

        }
        lock.writeLock().lock();

    }

}
