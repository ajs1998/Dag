package me.alexjs.dag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@Timeout(1)
public class TestTraverser {

    private static TestingHelper helper;

    @BeforeAll
    static void init() {
        helper = new TestingHelper();
    }

    @Test
    public void testTraverse() {

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

        helper.assertOrder(dag, sorted);

    }

}
