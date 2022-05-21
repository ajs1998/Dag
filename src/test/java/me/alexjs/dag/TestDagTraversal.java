package me.alexjs.dag;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

// No test should take longer than a second
//@Timeout(1)
public class TestDagTraversal {

    private static TestingHelper helper;

    public static void main(String[] args) throws InterruptedException {
        init();
        TestDagTraversal testDagTraversal = new TestDagTraversal();
        testDagTraversal.testMultiThreadTraverse();
    }

    @BeforeAll
    public static void init() {
        helper = new TestingHelper();
    }

    @RepeatedTest(2)
    public void testMultiThreadTraverse() throws InterruptedException {

        Dag<Integer> dag = helper.populateFlakyDag();
        BlockingQueue<Integer> sorted = new LinkedBlockingQueue<>();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        DagIterator<Integer> it = new DagIterator<>(dag);
        while (it.hasNext()) {
            final int i = it.next();
            executorService.submit(() -> {
                try {
                    sorted.add(i);
                } finally {
                    it.pushParents(i);
                }
            });
        }

        executorService.shutdown();

        Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.SECONDS));

        helper.assertOrder(dag, new LinkedList<>(sorted));

    }

    @Test
    public void testTraversalAbort() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        DagIterator<Integer> it = new DagIterator<>(dag);
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        AtomicBoolean aborted = new AtomicBoolean(false);

        while (it.hasNext() && !aborted.get()) {
            final int i = it.next();
            executorService.submit(() -> {
                try {
                    if (true) {
                        throw new RuntimeException("uh oh");
                    }
                    it.pushParents(i);
                } catch (Throwable t) {
                    throwable.compareAndSet(null, t);
                    aborted.set(true);
                }
            });
        }

        executorService.shutdown();

        Assertions.assertTrue(executorService.awaitTermination(1, TimeUnit.SECONDS));

        Assertions.assertTrue(executorService.isShutdown());
        Assertions.assertTrue(executorService.isTerminated());

    }

    @RepeatedTest(50)
    public void testSingleThreadTraverse() {

        Dag<Integer> dag = helper.populateDag();
        List<Integer> sorted = new LinkedList<>();

        DagIterator<Integer> it = new DagIterator<>(dag);
        while (it.hasNext()) {
            int i = it.next();
            sorted.add(i);
            it.pushParents(i);
        }

        helper.assertOrder(dag, sorted);

    }

    @RepeatedTest(50)
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

    @RepeatedTest(50)
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

    @RepeatedTest(50)
    public void testCollectionAddAll() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = new LinkedList<>();

        sorted.addAll(dag);
        Collections.reverse(sorted);
        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @RepeatedTest(50)
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
