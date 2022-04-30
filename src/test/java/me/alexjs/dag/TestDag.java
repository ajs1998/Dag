package me.alexjs.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class TestDag {

    private final Random random;

    public TestDag() {
        this.random = new Random();
    }

    @Test
    public void testSort() {

        Dag<Integer> dag = populateDag();

        List<Integer> sorted = dag.sort();
        Collections.reverse(sorted);

        assertOrder(dag, sorted);

        // Test HashDag's map constructor
        Dag<Integer> dag2 = new HashDag<>(dag.toMap());
        Assertions.assertEquals(dag, dag2);

    }

    @Disabled
    @Test
    public void testDagTraversal() {

        Dag<Integer> dag = populateDag();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Integer> sorted = new LinkedList<>();

        DagIterator<Integer> it = new DagIterator<>(dag);
        while (it.hasNext()) {
            final int i = it.next();
            executorService.submit(() -> {
                synchronized (sorted) {
                    sorted.add(i);
                }
                it.pushParents(i);
            });
        }

        assertOrder(dag, sorted);

    }

    @Test
    public void testCircularDependency() {

        // This DAG is guaranteed to have no circular dependencies
        Dag<Integer> dag = populateDag();

        // Add a long circular dependencies
        dag.put(0, 1);
        dag.put(1, 2);
        dag.put(2, 3);
        dag.put(3, 4);
        dag.put(4, 5);
        dag.put(5, 6);
        dag.put(6, 0);

        Assertions.assertNull(dag.sort());

    }

    @Test
    public void testExtremities() {

        Dag<Integer> dag = populateDag();

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

        Dag<Integer> dag = populateDag();

        int node = getMiddleNode(dag);

        Set<Integer> ancestors = dag.getAncestors(node);
        Assertions.assertFalse(ancestors.isEmpty());
        for (Integer ancestor : ancestors) {
            Assertions.assertTrue(ancestor < node);
        }
        Assertions.assertTrue(ancestors.containsAll(dag.getParents(node)));

        Set<Integer> descendants = dag.getDescendants(node);
        Assertions.assertFalse(descendants.isEmpty());
        for (Integer descendant : descendants) {
            Assertions.assertTrue(descendant > node);
        }
        Assertions.assertTrue(descendants.containsAll(dag.getChildren(node)));

    }

    @Test
    public void testEmptyDag() throws Throwable {

        Dag<Integer> dag = new HashDag<>();

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

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        DagIterator<Integer> it = new DagIterator<>(dag);
        while (it.hasNext()) {
            // Peek a node in the iterator
            Integer i = it.next();

            // Submit a task for this node
            executorService.submit(() -> {
                Assertions.fail("This shouldn't happen");
                it.pushParents(i);
            });
        }

    }

    @Test
    public void testNoChildren() {

        // Test with putAll()
        Dag<Integer> dag = new HashDag<>();
        dag.putAll(0, new HashSet<>());
        Map<Integer, Collection<Integer>> map = dag.toMap();

        Assertions.assertTrue(map.containsKey(0));
        Assertions.assertTrue(map.get(0).isEmpty());

        // Test with add()
        dag = new HashDag<>();
        dag.add(0);
        map = dag.toMap();

        Assertions.assertTrue(map.containsKey(0));
        Assertions.assertTrue(map.get(0).isEmpty());

    }

    private Dag<Integer> populateDag() {

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

    private void assertOrder(Dag<Integer> dag, List<Integer> sorted) {
        synchronized (sorted) {
            Assertions.assertEquals(dag.toMap().keySet().size(), sorted.size());
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
