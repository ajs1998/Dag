package me.alexjs.dag;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TraversableForest<T> extends SimpleForest<T> {

    public void traverse(Consumer<T> fn, ExecutorService executorService) {
        Map<T, Set<T>> copy = asMap();

        Set<T> leaves = getLeaves(copy);
        if (leaves.isEmpty()) {
            executorService.shutdown();
            return;
        }

        for (T node : leaves) {
            visit(copy, fn, node, executorService);
        }
    }

    private void visit(Map<T, Set<T>> forest, Consumer<T> fn, T node, ExecutorService executorService) {
        if (!forest.get(node).isEmpty()) {
            // If node has children, then it is not ready to be visited
            return;
        }
        // Apply fn to node, remove node from the forest, then visit its parent nodes
        executorService.submit(() -> {
            fn.accept(node);
            synchronized (forest) {
                forest.remove(node);
                for (T parent : getParents(forest, node)) {
                    forest.get(parent).remove(node);
                    visit(forest, fn, parent, executorService);
                }
                if (forest.isEmpty()) {
                    executorService.shutdown();
                }
            }
        });
    }

    private Set<T> getLeaves(Map<T, Set<T>> forest) {
        Set<T> leaves = new HashSet<>();
        for (Map.Entry<T, Set<T>> entry : forest.entrySet()) {
            if (entry.getValue().isEmpty()) {
                leaves.add(entry.getKey());
            }
        }
        return leaves;
    }

    private Set<T> getParents(Map<T, Set<T>> forest, T child) {
        return forest.entrySet().stream()
                .filter(e -> e.getValue().contains(child))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
