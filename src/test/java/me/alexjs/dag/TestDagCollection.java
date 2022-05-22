package me.alexjs.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestDagCollection {

    private static TestingHelper helper;

    @BeforeAll
    public static void init() {
        helper = new TestingHelper();
    }

    @Test
    public void testRemoveAndRetain() {

        Dag<Integer> dag = new HashDag<>();

        Assertions.assertFalse(dag.remove(0));
        Assertions.assertFalse(dag.removeAll(List.of(1, 2, 3)));
        Assertions.assertFalse(dag.retainAll(List.of(4, 5, 6)));

        dag.add(1);
        Assertions.assertTrue(dag.remove(1));
        Assertions.assertTrue(dag.isEmpty());

        dag.put(2, 3);
        dag.put(2, 4);
        Assertions.assertTrue(dag.removeAll(List.of(2, 3, 4)));
        Assertions.assertTrue(dag.isEmpty());

        dag.put(5, 6);
        dag.put(7, 6);
        Assertions.assertTrue(dag.removeAll(List.of(5, 6, 7)));
        Assertions.assertTrue(dag.isEmpty());

        dag.put(10, 11);
        dag.put(10, 12);
        dag.put(13, 14);
        dag.put(15, 14);
        Assertions.assertEquals(2, dag.getChildren(10).size());
        Assertions.assertEquals(2, dag.getParents(14).size());

        Assertions.assertTrue(dag.retainAll(List.of(10, 14)));
        Assertions.assertEquals(0, dag.getChildren(10).size());
        Assertions.assertEquals(0, dag.getParents(14).size());

    }

    @Test
    public void testSize() {

        Dag<Integer> dag = helper.populateDag();

        Assertions.assertTrue(dag.size() > 0);
        Assertions.assertFalse(dag.isEmpty());

        dag.clear();

        Assertions.assertEquals(0, dag.size());
        Assertions.assertTrue(dag.isEmpty());

    }

    @Test
    public void testClone() {

        Dag<Integer> dag = helper.populateDag();
        Dag<Integer> clone = dag.clone();

        // Make sure they do not reference the same object
        Assertions.assertNotSame(clone, dag);

        // Make sure their class is the same
        Assertions.assertSame(clone.getClass(), dag.getClass());

        // Make sure their contents are equivalent
        Assertions.assertEquals(clone, dag);
        Assertions.assertEquals(clone.hashCode(), dag.hashCode());

        // Modify the original, then make sure that their contents are not equivalent anymore
        dag.add(Integer.MAX_VALUE);
        Assertions.assertTrue(dag.contains(Integer.MAX_VALUE));
        Assertions.assertTrue(dag.containsAll(clone.getNodes()));
        Assertions.assertNotEquals(clone, dag);
        Assertions.assertNotEquals(clone.hashCode(), dag.hashCode());

        // Bonus: test HashDag's map constructor
        Dag<Integer> copy = new HashDag<>(dag.toMap());
        Assertions.assertEquals(copy, dag);
        Assertions.assertSame(copy.getClass(), dag.getClass());
        Assertions.assertEquals(copy, dag);
        Assertions.assertEquals(copy.hashCode(), dag.hashCode());

    }

    @Test
    public void testNulls() {

        Dag<Integer> dag = new HashDag<>();

        dag.add(null);

        dag.put(0, null);

        dag.put(null, 1);

        Assertions.assertEquals(3, dag.size());

        List<Integer> sorted = dag.sort();
        helper.assertOrder(dag, sorted);

        Assertions.assertTrue(dag.remove(null));
        Assertions.assertEquals(2, dag.size());

    }


}
