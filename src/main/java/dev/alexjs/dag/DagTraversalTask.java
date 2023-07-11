package dev.alexjs.dag;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * A helper class to easily traverse the nodes of a DAG with multiple threads.
 * Each node's task is only submitted for execution once all its ancestor nodes have finished execution,
 * or if it has no ancestors.
 * If the task applied to any node throws an exception,
 * then this will stop submitting new tasks
 * and {@link DagTraversalTask#awaitTermination(long, TimeUnit)} will return {@code false}.
 * <p>
 * The type parameter is not useful after the constructor is called,
 * so you could use {@code DagTraversalTask<?>} as your variable type.
 *
 * @param <T> the node type
 */
public class DagTraversalTask<T> {

    private final Dag<T> dag;
    private final Consumer<T> task;
    private final ListeningExecutorService executorService;
    private final Map<T, Set<T>> outgoingNodes;
    private final Lock lock;
    private final Condition terminated;
    private final AtomicReference<Status> status;

    /**
     * Create a task that traverses a DAG with an {@link java.util.concurrent.ExecutorService}
     * <p>
     * The nodes will be traversed in topological order,
     * such that no node is visited until all its incoming nodes have been visited.
     *
     * @param dag             the DAG to traverse
     * @param task            the task to apply to each node
     * @param executorService the {@link ExecutorService} to submit these tasks to
     */
    public DagTraversalTask(Dag<T> dag, Consumer<T> task, ExecutorService executorService) {

        this.dag = dag.clone();
        this.task = task;
        this.executorService = MoreExecutors.listeningDecorator(executorService);
        this.outgoingNodes = new HashMap<>();
        this.lock = new ReentrantLock(true);
        this.terminated = lock.newCondition();
        this.status = new AtomicReference<>(Status.RUNNING);

        // Cache each node's outgoing nodes for this DAG
        this.dag.getNodes().forEach(node -> this.outgoingNodes.put(node, this.dag.getOutgoing(node)));

        // Get the set of roots for this dag
        Set<T> roots = this.dag.getRoots();

        // If there are no leaves, then there are no nodes to visit
        if (roots.isEmpty()) {
            status.set(Status.DONE);
        } else {
            visit(roots);
        }

    }

    /**
     * Blocks until all nodes have been traversed, or the timeout occurs,
     * or a task fails, or the current thread is interrupted, whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if the traversal is terminated and {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {

        Status currentStatus = status.get();
        if (currentStatus == Status.DONE) {
            return true;
        } else if (currentStatus == Status.ERROR) {
            return false;
        }

        try {
            lock.lock();
            return terminated.await(timeout, unit) && status.get() == Status.DONE;
        } finally {
            lock.unlock();
        }

    }

    private void visit(Collection<T> nodes) {
        for (final T node : nodes) {
            executorService.submit(() -> run(node))
                    .addListener(() -> propagate(node), executorService);
        }
    }

    private void run(T node) {
        try {
            task.accept(node);
        } catch (Throwable t) {
            try {
                lock.lock();
                status.compareAndSet(Status.RUNNING, Status.ERROR);
                terminated.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    private void propagate(T node) {
        try {
            lock.lock();

            dag.remove(node);
            if (dag.isEmpty()) {
                status.compareAndSet(Status.RUNNING, Status.DONE);
                terminated.signalAll();
            }

            Set<T> outgoing = this.outgoingNodes.get(node);
            outgoing.retainAll(dag.getNodes());
            outgoing.removeIf(p -> !dag.getIncoming(p).isEmpty());

            visit(outgoing);
        } finally {
            lock.unlock();
        }
    }

    private enum Status {
        RUNNING,
        ERROR,
        DONE
    }

}
