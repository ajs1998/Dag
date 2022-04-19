package me.alexjs.dag;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DagUtil {

    public static <T> void traverse(Dag<T> dag, Consumer<T> fn, ExecutorService executorService) throws Throwable {

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
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        for (T node : leaves) {
            try {
                visit(dagMap, fn, node, executorService, throwable);
            } catch (RejectedExecutionException ex) {
                // Throw the first exception that killed a worker instead of this RejectedExecutionException
                throw throwable.get();
            }
        }

    }

    private static <T> void visit(Map<T, Set<T>> dag, Consumer<T> fn, T node, ExecutorService executorService, AtomicReference<Throwable> throwable) {

        // If node has children, then it is not ready to be visited
        if (!dag.get(node).isEmpty()) {
            return;
        }

        // Apply fn to node, remove node from the forest, then visit its parent nodes
        executorService.submit(() -> {
            try {
                fn.accept(node);
            } catch (Throwable t) {
                // If this function fails, kill the ExecutorService and save the exception to rethrow later
                executorService.shutdownNow();
                throwable.compareAndSet(null, t);
                return;
            }
            synchronized (dag) {
                dag.remove(node);
                for (Map.Entry<T, Set<T>> e : dag.entrySet()) {
                    if (e.getValue().contains(node)) {
                        T parent = e.getKey();
                        dag.get(parent).remove(node);
                        visit(dag, fn, parent, executorService, throwable);
                    }
                }
                if (dag.isEmpty()) {
                    executorService.shutdown();
                }
            }
        });

    }

}
