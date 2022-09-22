package me.alexjs.dag;

import org.junit.jupiter.api.*;

import java.util.*;

@Timeout(2)
public class TestDag {

    private static TestingHelper helper;

    @BeforeAll
    public static void init() {
        helper = new TestingHelper();
    }

    @RepeatedTest(50)
    public void testSort() {

        Dag<Integer> dag = helper.populateDagSimple();

        List<Integer> sorted = dag.sort();

        helper.assertOrder(dag, sorted);

    }

    @Test
    public void testCircularDependency() {

        // This DAG is guaranteed to have no circular dependencies
        Dag<Integer> dag = helper.populateDag();

        // Add a long chain of circular dependencies
        dag.put(0, 1);
        dag.put(1, 2);
        dag.put(2, 3);
        dag.put(3, 4);
        dag.put(4, 5);
        dag.put(5, 6);
        dag.put(6, 0);

        Assertions.assertNull(dag.sort());

        Assertions.assertThrows(IllegalArgumentException.class, () -> dag.getAncestors(3));
        Assertions.assertThrows(IllegalArgumentException.class, () -> dag.getDescendants(3));

        Assertions.assertDoesNotThrow(() -> dag.getIncoming(3));
        Assertions.assertDoesNotThrow(() -> dag.getOutgoing(3));
        Assertions.assertDoesNotThrow(dag::getRoots);
        Assertions.assertDoesNotThrow(dag::getLeaves);

    }

    @Test
    public void testExtremities() {

        Dag<Integer> dag = helper.populateDag();

        Set<Integer> roots = dag.getRoots();
        for (Integer root : roots) {
            Assertions.assertTrue(dag.getIncoming(root).isEmpty());
        }

        Set<Integer> leaves = dag.getLeaves();
        for (Integer leaf : leaves) {
            Assertions.assertTrue(dag.getOutgoing(leaf).isEmpty());
        }

    }

    @Test
    public void testAncestry() {

        Dag<Integer> dag = helper.populateDag();

        int node = helper.getMiddleNode(dag);

        Set<Integer> ancestors = dag.getAncestors(node);
        Assertions.assertFalse(ancestors.isEmpty());
        for (Integer ancestor : ancestors) {
            Assertions.assertTrue(ancestor < node);
        }
        Assertions.assertTrue(ancestors.containsAll(dag.getIncoming(node)));

        Set<Integer> descendants = dag.getDescendants(node);
        Assertions.assertFalse(descendants.isEmpty());
        for (Integer descendant : descendants) {
            Assertions.assertTrue(descendant > node);
        }
        Assertions.assertTrue(descendants.containsAll(dag.getOutgoing(node)));

    }

    @Test
    public void testEmptyDag() {

        Dag<Integer> dag = new HashDag<>();

        Set<Integer> roots = dag.getRoots();
        Set<Integer> leaves = dag.getLeaves();
        Set<Integer> ancestors = dag.getAncestors(0);
        Set<Integer> descendants = dag.getDescendants(0);
        Set<Integer> incoming = dag.getIncoming(0);
        Set<Integer> outgoing = dag.getOutgoing(0);

        Assertions.assertTrue(roots.isEmpty());
        Assertions.assertTrue(leaves.isEmpty());
        Assertions.assertTrue(ancestors.isEmpty());
        Assertions.assertTrue(descendants.isEmpty());
        Assertions.assertTrue(incoming.isEmpty());
        Assertions.assertTrue(outgoing.isEmpty());

        Iterator<Integer> it = dag.iterator();
        Assertions.assertFalse(it.hasNext());

    }

    @Test
    public void testNoOutgoing() {

        // Test with putAll()
        Dag<Integer> dag = new HashDag<>();
        dag.putAll(0, new HashSet<>());
        Map<Integer, Collection<Integer>> map = dag.toMap();

        Assertions.assertTrue(map.containsKey(0));
        Assertions.assertTrue(map.get(0).isEmpty());

        // Test with add()
        dag = new HashDag<>();
        Assertions.assertTrue(dag.add(0));
        Assertions.assertFalse(dag.add(0));
        Assertions.assertTrue(dag.put(1, 2));
        Assertions.assertTrue(dag.put(1, 3));
        Assertions.assertTrue(dag.put(4, 1));
        Assertions.assertFalse(dag.put(1, 2));
        map = dag.toMap();

        Assertions.assertTrue(map.containsKey(0));
        Assertions.assertTrue(map.get(0).isEmpty());

    }

}
