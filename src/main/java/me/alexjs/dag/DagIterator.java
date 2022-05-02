package me.alexjs.dag;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
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
    private final Map<T, Set<T>> parents;
    private final Lock w;
    private final Lock r;

    /**
     * Create a new {@link DagIterator} for a given {@link Dag}.
     * <p>
     * This iterator will return each node such that {@link DagIterator#pushParents} has already been called for all of
     * its children nodes.
     *
     * @param dag the DAG to iterate over
     */
    public DagIterator(final Dag<T> dag) {

        this.dag = dag.clone();
        this.queue = new LinkedBlockingQueue<>();
        this.parents = new HashMap<>();

        // Cache the parents of each node for this DAG
        for (T node : this.dag.getNodes()) {
            this.parents.put(node, this.dag.getParents(node));
        }

        // Get the set of leaves for this dag
        Set<T> leaves = this.dag.getLeaves();

        // If there are no leaves, then there are no nodes to visit
        if (!leaves.isEmpty()) {
            visit(leaves);
        }

        // Create a read/write lock for the two methods in here to use
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.w = lock.writeLock();
        this.r = lock.readLock();

    }

    @Override
    public boolean hasNext() {

        try {
            r.lock();
            return !queue.isEmpty() || !dag.isEmpty();
        } finally {
            r.unlock();
        }

    }

    @Override
    public T next() {

        try {

            return queue.take();

        } catch (InterruptedException e) {

            // TODO clear the queue, clear the DAG, hasNext = false
            throw new CompletionException(e);

        }

    }

    public void pushParents(T node) {

        w.lock();

        Set<T> enqueue = parents.get(node);

        enqueue.retainAll(dag.getNodes());
        enqueue.removeIf(p -> !dag.getChildren(p).isEmpty());

        visit(enqueue);

        w.unlock();

    }

    private void visit(Collection<T> enqueue) {
        queue.addAll(enqueue);
        dag.removeAll(enqueue);
    }

}
