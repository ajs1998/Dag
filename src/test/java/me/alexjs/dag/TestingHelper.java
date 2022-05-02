package me.alexjs.dag;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class TestingHelper {

    private final Random random;

    public TestingHelper() {

        // I want to give these tests the opportunity to flake
        // If tests flake because the random numbers it generates are just right,
        // then I want to be able to recreate it
        int seed = new Random().nextInt(1000);
        System.out.println("Random seed: " + seed);

        this.random = new Random(seed);

    }

    public void assertOrder(Dag<Integer> dag, List<Integer> sorted) {
        synchronized (sorted) {
            Assertions.assertEquals(dag.getNodes().size(), sorted.size());
            for (Integer parent : sorted) {
                // If a parent comes after any of its children, then fail
                dag.getChildren(parent).stream()
                        .filter(child -> sorted.indexOf(parent) <= sorted.indexOf(child))
                        .forEach(i -> Assertions.fail());
            }
        }
    }

    public int getMiddleNode(Dag<Integer> dag) {
        int candidate = 250;
        while (dag.getChildren(candidate).isEmpty() || dag.getParents(candidate).isEmpty()) {
            candidate++;
            Assertions.assertTrue(candidate < 1000);
        }
        return candidate;
    }

    public Dag<Integer> populateDag() {

        // Add a ton of parent-child relationships. Many nodes will have multiple children
        Dag<Integer> dag = new HashDag<>();
        int nodes = random.nextInt(5000) + 5000;
        for (int i = 0; i < nodes; i++) {
            // A parent will always be strictly less than its children to ensure no circular dependencies
            int parent = random.nextInt(500);
            int child = parent + random.nextInt(500) + 1;
            dag.put(parent, child);
        }

        // Add some extra nodes that are guaranteed to have no parents or children
        ArrayList<Integer> extra = IntStream.generate(() -> random.nextInt(2000) + 1000)
                .limit(100)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        dag.addAll(extra);

        return dag;

    }

    public Dag<Integer> populateDagSimple() {

        Dag<Integer> dag = new HashDag<>();

        dag.put(1, 5);
        dag.put(2, 5);
        dag.put(2, 6);
        dag.put(3, 6);
        dag.put(4, 6);
        dag.put(4, 7);
        dag.put(5, 8);
        dag.put(6, 8);
        dag.put(7, 8);
        dag.put(7, 9);

        return dag;

    }

}
