package me.alexjs.dag;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * A helper class to easily traverse the nodes of a DAG with multiple threads.
 * Each node's task is only submitted for execution once all its children have finished execution, or if it has no
 * children.
 *
 * @param <T> the node type. This type parameter is not useful after the constructor is called, so you could use
 *            {@code DagTraversalTask<?>} as your variable type.Ω
 */
public class DagTraversalTask<T> {

    private final Dag<T> dag;
    private final Consumer<T> task;
    private final ListeningExecutorService executorService;
    private final Map<T, Set<T>> parents;
    private final Lock lock;

    /**
     * Create a task that traverses a DAG with an {@link java.util.concurrent.ExecutorService}
     * <p>
     * The nodes will be traversed in reverse-topological order,
     * such that no node is visited until all its children have been visited.
     *
     * @param dag             the DAG to traverse
     * @param task            the task to apply to each node
     * @param executorService the {@link ExecutorService} to submit these tasks to
     */
    public DagTraversalTask(Dag<T> dag, Consumer<T> task, ExecutorService executorService) {

        this.dag = dag.clone();
        this.task = task;
        this.executorService = MoreExecutors.listeningDecorator(executorService);
        this.parents = new HashMap<>();
        this.lock = new ReentrantLock(true);

        // Cache the parents of each node for this DAG
        this.dag.getNodes().forEach(node -> this.parents.put(node, this.dag.getParents(node)));

        // Get the set of leaves for this dag
        Set<T> leaves = this.dag.getLeaves();

        // If there are no leaves, then there are no nodes to visit
        if (leaves.isEmpty()) {
            executorService.shutdown();
        } else {
            visit(leaves);
        }

    }

    /**
     * Blocks until all nodes have been traversed, or the timeout occurs, or the current thread is interrupted,
     * whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if the traversal is terminated and {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    private void visit(Collection<T> nodes) {

        for (final T node : nodes) {
            executorService.submit(() -> task.accept(node))
                    .addListener(() -> {
                        lock.lock();

                        dag.remove(node);
                        if (dag.isEmpty()) {
                            executorService.shutdown();
                        }

                        Set<T> parents = this.parents.get(node);
                        parents.retainAll(dag.getNodes());
                        parents.removeIf(p -> !dag.getChildren(p).isEmpty());

                        lock.unlock();

                        visit(parents);

                    }, MoreExecutors.directExecutor());
        }

    }

}
