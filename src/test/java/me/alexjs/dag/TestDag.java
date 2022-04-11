package me.alexjs.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestDag {

    private final Random random;

    public TestDag() {
        this.random = new Random(0);
    }

    @Test
    public void testSimpleForest() {

        Dag<Integer> dag = new SimpleForest<>();
        populateDag(dag);

        List<Integer> sorted = dag.topologicalSort();
        Collections.reverse(sorted);

        assertOrder(dag, sorted);

    }

    @Test
    public void testTraversableForest() throws InterruptedException {

        TraversableForest<Integer> dag = new TraversableForest<>();
        populateDag(dag);

        List<Integer> sorted = new LinkedList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        dag.traverse(i -> {
            synchronized (sorted) {
                sorted.add(i);
            }
        }, executorService);

        Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.SECONDS));

        assertOrder(dag, sorted);

    }

    @Test
    public void testExtremities() {

        Dag<Integer> dag = new SimpleForest<>();
        populateDag(dag);

        Set<Integer> roots = dag.getRoots();
        for (Integer root : roots) {
            Assertions.assertTrue(dag.getParents(root).isEmpty());
        }

        Set<Integer> leaves = dag.getLeaves();
        for (Integer leaf : leaves) {
            Assertions.assertTrue(dag.getChildren(leaf).isEmpty());
        }

    }

    @Test
    public void testAncestry() {

        Dag<Integer> dag = new SimpleForest<>();
        populateDag(dag);

        int node = getMiddleNode(dag);

        Set<Integer> ancestors = dag.getAncestors(node);
        Assertions.assertFalse(ancestors.isEmpty());
        for (Integer ancestor : ancestors) {
            Assertions.assertTrue(ancestor < node);
        }

        Set<Integer> descendants = dag.getDescendants(node);
        Assertions.assertFalse(descendants.isEmpty());
        for (Integer descendant : descendants) {
            Assertions.assertTrue(descendant > node);
        }

    }

    @Test
    public void testEmptyDag() {

        Dag<Integer> dag = new SimpleForest<>();

        Set<Integer> roots = dag.getRoots();
        Set<Integer> leaves = dag.getLeaves();
        Set<Integer> ancestors = dag.getAncestors(0);
        Set<Integer> descendants = dag.getDescendants(0);
        Set<Integer> parents = dag.getParents(0);
        Set<Integer> children = dag.getChildren(0);

        Assertions.assertTrue(roots.isEmpty());
        Assertions.assertTrue(leaves.isEmpty());
        Assertions.assertTrue(ancestors.isEmpty());
        Assertions.assertTrue(descendants.isEmpty());
        Assertions.assertTrue(parents.isEmpty());
        Assertions.assertTrue(children.isEmpty());

    }

    private void populateDag(Dag<Integer> dag) {
        int nodes = random.nextInt(5000) + 5000;
        for (int i = 0; i < nodes; i++) {
            // A parent will always be strictly less than its children to ensure no circular dependencies
            int parent = random.nextInt(500);
            int child = parent + random.nextInt(500) + 1;
            dag.add(parent, child);
        }
    }

    private void assertOrder(Dag<Integer> dag, List<Integer> sorted) {
        synchronized (sorted) {
            for (Integer parent : sorted) {
                // If a parent comes after any of its children, then fail
                dag.getChildren(parent).stream()
                        .filter(child -> sorted.indexOf(parent) <= sorted.indexOf(child))
                        .forEach(i -> Assertions.fail());
            }
        }
    }

    private int getMiddleNode(Dag<Integer> dag) {
        int candidate = 250;
        while (dag.getChildren(candidate).isEmpty() || dag.getParents(candidate).isEmpty()) {
            candidate++;
            Assertions.assertTrue(candidate < 1000);
        }
        return candidate;
    }

}
