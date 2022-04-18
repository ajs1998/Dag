package me.alexjs.dag;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class DagUtil {

    public static <T> void traverse(Dag<T> dag, Consumer<T> fn, ExecutorService executorService) throws Throwable {

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

        AtomicReference<Throwable> throwable = new AtomicReference<>();
        for (T node : leaves) {
            try {
                visit(copy, fn, node, executorService, throwable);
            } catch (RejectedExecutionException ex) {
                throw throwable.get();
            }
        }

    }

    private static <T> void visit(Map<T, Set<T>> forest, Consumer<T> fn, T node, ExecutorService executorService, AtomicReference<Throwable> throwable) throws Throwable {

        // If node has children, then it is not ready to be visited
        if (!forest.get(node).isEmpty()) {
            return;
        }

        // Apply fn to node, remove node from the forest, then visit its parent nodes
        executorService.submit(() -> {
            try {
                fn.accept(node);
            } catch (Throwable t) {
                executorService.shutdownNow();
                if (throwable.get() == null) {
                    throwable.set(t);
                }
                return;
            }
            synchronized (forest) {
                forest.remove(node);
                for (Map.Entry<T, Set<T>> e : forest.entrySet()) {
                    if (e.getValue().contains(node)) {
                        T parent = e.getKey();
                        forest.get(parent).remove(node);
                        try {
                            visit(forest, fn, parent, executorService, throwable);
                        } catch (Throwable ignore) {
                        }
                    }
                }
                if (forest.isEmpty()) {
                    executorService.shutdown();
                }
            }
        });

    }

}
