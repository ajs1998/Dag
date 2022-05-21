package me.alexjs.dag;

import java.util.*;
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
    private final Map<T, Set<T>> parents;
    private final Lock lock;

    /**
     * Create a new {@link DagIterator} for a given {@link Dag}
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
        if (!queue.isEmpty() || !this.dag.isEmpty()) {
            visit(leaves);
        }

        this.lock = new ReentrantLock(true);

    }

    @Override
    public boolean hasNext() {

        try {
            lock.lock();
            return !queue.isEmpty() || !dag.isEmpty();
        } finally {
            lock.unlock();
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

        lock.lock();

        Set<T> enqueue = parents.get(node);

        enqueue.retainAll(dag.getNodes());
        enqueue.removeIf(p -> !dag.getChildren(p).isEmpty());

        visit(enqueue);

        lock.unlock();

    }

    private void visit(Collection<T> enqueue) {
        queue.addAll(enqueue);
        dag.removeAll(enqueue);
    }

}
