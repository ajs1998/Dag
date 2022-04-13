package me.alexjs.dag;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class DagUtil {

    public static <T> void traverse(Dag<T> dag, Consumer<T> fn, ExecutorService executorService) {

        Map<T, Set<T>> copy = dag.asMap();

        Set<T> leaves = new HashSet<>();
        for (Map.Entry<T, Set<T>> entry : copy.entrySet()) {
            if (entry.getValue().isEmpty()) {
                leaves.add(entry.getKey());
            }
        }

        if (leaves.isEmpty()) {
            executorService.shutdown();
            return;
        }

        for (T node : leaves) {
            visit(copy, fn, node, executorService);
        }

    }

    private static <T> void visit(Map<T, Set<T>> forest, Consumer<T> fn, T node, ExecutorService executorService) {

        // If node has children, then it is not ready to be visited
        if (!forest.get(node).isEmpty()) {
            return;
        }

        // Apply fn to node, remove node from the forest, then visit its parent nodes
        executorService.submit(() -> {
            fn.accept(node);
            synchronized (forest) {
                forest.remove(node);
                for (Map.Entry<T, Set<T>> e : forest.entrySet()) {
                    if (e.getValue().contains(node)) {
                        T parent = e.getKey();
                        forest.get(parent).remove(node);
                        visit(forest, fn, parent, executorService);
                    }
                }
                if (forest.isEmpty()) {
                    executorService.shutdown();
                }
            }
        });

    }

}
