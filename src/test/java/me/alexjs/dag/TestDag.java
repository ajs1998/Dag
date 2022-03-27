package me.alexjs.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestDag {

    private final Random random;

    public TestDag() {
        this.random = new Random();
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
        dag.traverse(sorted::add, executorService);

        Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.SECONDS));

        assertOrder(dag, sorted);

    }

    private void assertOrder(Dag<Integer> dag, List<Integer> sorted) {
        for (Integer parent : sorted) {
            // If a parent comes after any of its children, then fail
            dag.getChildren(parent).stream()
                    .filter(child -> sorted.indexOf(parent) <= sorted.indexOf(child))
                    .forEach(i -> Assertions.fail());
        }
    }

    private void populateDag(Dag<Integer> dag) {
        int bound = random.nextInt(5000) + 5000;
        for (int i = 0; i < bound; i++) {
            // A parent will always be strictly less than its children to ensure no circular dependencies
            int parent = random.nextInt(500);
            int child = parent + random.nextInt(500) + 1;
            dag.add(parent, child);
        }
    }

}
