package me.alexjs.dag;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Helper class for interacting with a DAG
 */
public class DagUtil {

    /**
     * Traverse a DAG. The given {@code function} will be applied to all nodes. Each node visited either has no children
     * or all of its children have already been visited. An ExecutorService will submit a node visit task for execution.
     * When all nodes have been visited, the ExecutorService will be shut down. When all visits have finished executing
     * after shutdown, the ExecutorService will be terminated.
     *
     * @param dag             the DAG to traverse
     * @param function        the function to apply to each node
     * @param executorService the ExecutorService that node visit tasks are submitted to
     * @param <T>             the type of DAG to traverse
     * @throws Exception the first exception thrown by {@code function}, if any
     */
    public static <T> void traverse(Dag<T> dag, Consumer<T> function, ExecutorService executorService) throws Exception {

        // Work with a clone so the original DAG structure is untouched
        Dag<T> copy = dag.clone();

        // Get the leaves
        Set<T> leaves = copy.getLeaves();

        // If there are no leaves (empty DAG), then shutdown the ExecutorService and return
        if (leaves.isEmpty()) {
            executorService.shutdown();
            return;
        }

        Map<T, Set<T>> dagMap = copy.asMap();
        AtomicReference<Exception> exception = new AtomicReference<>();
        for (T node : leaves) {
            try {
                visit(dagMap, function, node, executorService, exception);
            } catch (RejectedExecutionException ignore) {
                // Throw the first exception that killed a worker instead of this useless RejectedExecutionException
                throw exception.get();
            }
        }

    }

    private static <T> void visit(Map<T, Set<T>> dag, Consumer<T> fn, T node, ExecutorService executorService, AtomicReference<Exception> exception) {

        // If node has children, then it is not ready to be visited
        if (!dag.get(node).isEmpty()) {
            return;
        }

        // Apply fn to node, remove node from the DAG, then visit its parent nodes
        executorService.submit(() -> {
            try {
                fn.accept(node);
            } catch (Exception e) {
                // If this function fails, kill the ExecutorService and save the exception to rethrow later
                executorService.shutdownNow();
                exception.compareAndSet(null, e);
                return;
            }
            synchronized (dag) {
                dag.remove(node);
                for (Map.Entry<T, Set<T>> e : dag.entrySet()) {
                    if (e.getValue().contains(node)) {
                        T parent = e.getKey();
                        dag.get(parent).remove(node);
                        visit(dag, fn, parent, executorService, exception);
                    }
                }
                if (dag.isEmpty()) {
                    executorService.shutdown();
                }
            }
        });

    }

}
