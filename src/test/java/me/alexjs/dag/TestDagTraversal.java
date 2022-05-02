package me.alexjs.dag;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// No test should take longer than a second
@Timeout(1)
public class TestDagTraversal {

    private static TestingHelper helper;

    @BeforeAll
    public static void init() {
        helper = new TestingHelper();
    }

    @Test
    public void testMultiThreadTraverse() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        DagTraverser<Integer> traverser = new DagTraverser<>(dag);
        List<Integer> sorted = new LinkedList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Optional<Integer> optional;
        while ((optional = traverser.get()).isPresent()) {
            final int i = optional.get();
            executorService.submit(() -> {
                synchronized (sorted) {
                    sorted.add(i);
                }
                traverser.done(i);
            });
        }

        executorService.shutdown();

        Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.SECONDS));

        helper.assertOrder(dag, sorted);

    }

    @Test
    public void testSingleThreadTraverse() {

        Dag<Integer> dag = helper.populateDag();
        DagTraverser<Integer> traverser = new DagTraverser<>(dag);
        List<Integer> sorted = new LinkedList<>();

        Optional<Integer> optional;
        while ((optional = traverser.get()).isPresent()) {
            final int i = optional.get();
            sorted.add(i);
            traverser.done(i);
        }

        helper.assertOrder(dag, sorted);

    }

    @Test
    public void testWhileIterator() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = new LinkedList<>();

        // Test the iterator with a while loop
        Iterator<Integer> it = dag.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            sorted.add(next);
        }
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testForIterator() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = new LinkedList<>();

        for (Integer next : dag) {
            sorted.add(next);
        }
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testCollectionAddAll() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = new LinkedList<>();

        sorted.addAll(dag);
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testToArray() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = new LinkedList<>();

        Collections.addAll(sorted, dag.toArray(new Integer[0]));
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

        sorted.clear();

        Collections.addAll(sorted, dag.toArray(new Integer[999999]));
        sorted = sorted.subList(0, sorted.indexOf(null));
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

}
