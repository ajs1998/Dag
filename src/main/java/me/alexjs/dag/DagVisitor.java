package me.alexjs.dag;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class DagVisitor<T> implements Supplier<Optional<T>> {

    private final Dag<T> dag;
    private final BlockingQueue<T> queue;
    private final Map<T, Set<T>> parents;
    private final Lock w;
    private final Lock r;

    private boolean complete;

    public DagVisitor(Dag<T> dag) {

        this.dag = dag.clone();
        this.queue = new LinkedBlockingQueue<>();
        this.parents = new HashMap<>();

        // Cache the parents of each node for this DAG
        for (T node : this.dag.getNodes()) {
            this.parents.put(node, this.dag.getParents(node));
        }

        // Get a set of the leaves of this dag
        Set<T> leaves = this.dag.getLeaves();

        // If there are no leaves, then there are no nodes to visit
        complete = leaves.isEmpty();

        if (!complete) {

            // Immediately "visit" the leaves
            this.queue.addAll(leaves);
            this.dag.removeAll(leaves);

        }

        // Create a read/write lock for the two methods in here to use
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.w = lock.writeLock();
        this.r = lock.readLock();


    }

    @Override
    public Optional<T> get() {

        if (complete) {
            return Optional.empty();
        }

        T node;
        try {
            node = queue.take();
        } catch (InterruptedException e) {
            // TODO clear the queue, clear the DAG, hasNext = false
            throw new CompletionException(e);
        }

        r.lock();
        if (dag.isEmpty() && queue.isEmpty()) {
            complete = true;
        }
        r.unlock();

        return Optional.of(node);

    }

    // nodes that have not been added to the queue yet
    // nodes that have not had done() called yet

    public void done(T node) {

        w.lock();

        Set<T> enqueue = parents.get(node);

        enqueue.retainAll(dag.getNodes());
        enqueue.removeIf(p -> !dag.getChildren(p).isEmpty());

        queue.addAll(enqueue);
        dag.removeAll(enqueue);

        w.unlock();

    }

}
