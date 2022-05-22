package me.alexjs.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

// No test should take longer than a second
@Timeout(1)
public class TestDagTraversal {

    private static TestingHelper helper;

    @BeforeAll
    public static void init() {
        helper = new TestingHelper();
    }

    @RepeatedTest(50)
    public void testMultiThreadTraverse() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        BlockingQueue<Integer> sorted = new LinkedBlockingQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        DagTraverser<?> traverser = new DagTraverser<>(dag, sorted::add, executorService);
        Assertions.assertTrue(traverser.get(1, TimeUnit.SECONDS));

        Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.SECONDS));

        helper.assertOrder(dag, new LinkedList<>(sorted));

    }

    @RepeatedTest(50)
    public void testSingleThreadTraverse() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        List<Integer> sorted = new LinkedList<>();

        DagTraverser<?> traverser = new DagTraverser<>(dag, sorted::add, Executors.newSingleThreadExecutor());
        Assertions.assertTrue(traverser.get(1, TimeUnit.SECONDS));

        helper.assertOrder(dag, sorted);

    }

    @RepeatedTest(50)
    public void testTimeout() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();

        DagTraverser<?> traverser = new DagTraverser<>(dag, e -> {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ignore) {
            }
        }, Executors.newSingleThreadExecutor());
        Assertions.assertFalse(traverser.get(500, TimeUnit.MILLISECONDS));

    }

    @RepeatedTest(10)
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

    @RepeatedTest(10)
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

    @RepeatedTest(10)
    public void testCollectionAddAll() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = new LinkedList<>();

        sorted.addAll(dag);
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @RepeatedTest(10)
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
