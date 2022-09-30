package me.alexjs.dag;

import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Timeout(2)
public class TestDagTraversal {

    private static TestingHelper helper;

    @BeforeAll
    public static void init() {
        helper = new TestingHelper();
    }

    @RepeatedTest(1000)
    public void testMultiThreadTraverse() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        DagTraversalTask<?> task = new DagTraversalTask<>(dag, sorted::add, executorService);
        Assertions.assertTrue(task.awaitTermination(2, TimeUnit.SECONDS));
        executorService.shutdown();

        helper.assertOrder(dag, new LinkedList<>(sorted));

    }

    @RepeatedTest(100)
    public void testSingleThreadTraverse() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        DagTraversalTask<?> task = new DagTraversalTask<>(dag, sorted::add, executorService);
        Assertions.assertTrue(task.awaitTermination(2, TimeUnit.SECONDS));
        executorService.shutdown();

        helper.assertOrder(dag, sorted);

    }

    @Test
    public void testTimeout() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        DagTraversalTask<?> task = new DagTraversalTask<>(dag, e -> {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ignore) {
            }
        }, executorService);
        executorService.shutdown();

        Assertions.assertFalse(task.awaitTermination(500, TimeUnit.MILLISECONDS));

    }

    @Test
    public void testWhileIterator() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());

        // Test the iterator with a while loop
        Iterator<Integer> it = dag.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            sorted.add(next);
        }

        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testForIterator() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());

        for (Integer next : dag) {
            sorted.add(next);
        }

        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testCollectionAddAll() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());

        sorted.addAll(dag);

        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testToArray() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> copy = dag.clone();

        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());

        Collections.addAll(sorted, dag.toArray(new Integer[0]));

        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

        sorted.clear();

        Collections.addAll(sorted, dag.toArray(new Integer[999999]));
        sorted = sorted.subList(0, sorted.indexOf(null));

        helper.assertOrder(dag, sorted);
        Assertions.assertEquals(copy, dag);

    }

    @Test
    public void testTraverseEmptyDag() throws InterruptedException {

        Dag<Integer> dag = new HashDag<>();
        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        DagTraversalTask<?> task = new DagTraversalTask<>(dag, sorted::add, executorService);
        executorService.shutdown();

        Assertions.assertTrue(task.awaitTermination(2, TimeUnit.SECONDS));
        Assertions.assertTrue(sorted.isEmpty());

    }

    @Test
    public void testException() throws InterruptedException {

        Dag<Integer> dag = helper.populateDag();
        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        DagTraversalTask<?> task = new DagTraversalTask<>(dag, e -> {
            sorted.add(e);
            throw new RuntimeException();
        }, executorService);
        executorService.shutdown();

        Assertions.assertFalse(task.awaitTermination(2, TimeUnit.SECONDS));
        Assertions.assertNotEquals(dag.size(), sorted.size());

    }

    @Test
    public void testInterrupt() {

        Dag<Integer> dag = helper.populateDag();
        List<Integer> sorted = Collections.synchronizedList(new LinkedList<>());
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        DagTraversalTask<?> task = new DagTraversalTask<>(dag, i -> {
            while (true) ;
        }, executorService);
        executorService.shutdown();

        Thread thread = new Thread(() -> {
            Assertions.assertThrows(InterruptedException.class, () -> task.awaitTermination(2, TimeUnit.SECONDS));
        });
        thread.start();
        thread.interrupt();

        Assertions.assertNotEquals(dag.size(), sorted.size());

    }

}
