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
 * TODO
 * @param <T>
 */
public class DagTraverser<T> {

    private final Dag<T> dag;
    private final Consumer<T> task;
    private final ListeningExecutorService executorService;
    private final Map<T, Set<T>> parents;
    private final Lock lock;

    /**
     * TODO
     * @param dag
     * @param task
     * @param executorService
     */
    public DagTraverser(Dag<T> dag, Consumer<T> task, ExecutorService executorService) {

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
     * TODO
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean get(long timeout, TimeUnit unit) throws InterruptedException {
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
