package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * ONE set of tests for BOTH lists (they have the same interface IList).
 * java.util.ArrayList is allowed here: tests only, as the "expected result".
 */
abstract class IListContractTest {

    abstract IList create();

    private static void assertSameContent(List<Integer> expected, IList actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).intValue(), actual.get(i), "index " + i);
        }
    }

    // ---------------------------------------------------------------- edge cases

    @Test
    void emptyStructure() {
        IList l = create();
        assertEquals(0, l.size());
        assertFalse(l.contains(0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.add(1, 5));
    }

    @Test
    void addAtIndexZeroOnEmptyWorks() {
        IList l = create();
        l.add(0, 7);
        assertEquals(1, l.size());
        assertEquals(7, l.get(0));
    }

    @Test
    void singleElement() {
        IList l = create();
        l.add(42);
        assertEquals(1, l.size());
        assertEquals(42, l.get(0));
        assertTrue(l.contains(42));
        assertFalse(l.contains(41));
        assertEquals(42, l.remove(0));
        assertEquals(0, l.size());
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(0));
        l.add(5);                       // must still work after becoming empty again
        assertEquals(5, l.get(0));
    }

    @Test
    void invalidIndicesThrowIndexOutOfBounds() {
        IList l = create();
        for (int i = 0; i < 5; i++) {
            l.add(i);
        }
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(5));
        assertThrows(IndexOutOfBoundsException.class, () -> l.add(-1, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> l.add(6, 9));
        assertEquals(5, l.size(), "a failed operation must not change the size");
    }

    @Test
    void firstAndLastIndex() {
        IList l = create();
        for (int i = 0; i < 20; i++) {
            l.add(i);
        }
        l.add(0, -1);                   // first
        l.add(l.size(), 100);           // last (index == size is allowed)
        assertEquals(-1, l.get(0));
        assertEquals(100, l.get(l.size() - 1));
        assertEquals(-1, l.remove(0));
        assertEquals(100, l.remove(l.size() - 1));
        assertEquals(19, l.get(l.size() - 1));
        l.add(200);                     // tail / size must still be correct
        assertEquals(200, l.get(l.size() - 1));
    }

    @Test
    void insertInTheMiddleKeepsOrder() {
        IList l = create();
        l.add(10);
        l.add(20);
        l.add(30);
        l.add(1, 99);
        assertEquals(10, l.get(0));
        assertEquals(99, l.get(1));
        assertEquals(20, l.get(2));
        assertEquals(30, l.get(3));
    }

    @Test
    void duplicatesRemoveOnlyOneOccurrence() {
        IList l = create();
        l.add(3);
        l.add(3);
        l.add(3);
        assertTrue(l.contains(3));
        l.remove(1);
        assertEquals(2, l.size());
        assertTrue(l.contains(3));
        l.remove(0);
        l.remove(0);
        assertFalse(l.contains(3));
    }

    @Test
    void largeValuesAreComparedByValue() {
        IList l = create();                // guards against comparing boxed Integer objects with ==
        l.add(1000);
        l.add(2000000);
        assertTrue(l.contains(1000));
        assertTrue(l.contains(2000000));
    }

    @Test
    void growthBeyondInitialCapacityKeepsOrder() {
        IList l = create();
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            l.add(i * 3);
            expected.add(i * 3);
        }
        assertSameContent(expected, l);
    }

    // ---------------------------------------------------------------- correctness on random data

    @Test
    void randomOperationsMatchArrayList() {
        Random rnd = new Random(12345);
        IList l = create();
        List<Integer> model = new ArrayList<>();
        for (int step = 0; step < 20_000; step++) {
            int op = rnd.nextInt(5);
            int value = rnd.nextInt(50);
            switch (op) {
                case 0:
                    l.add(value);
                    model.add(value);
                    break;
                case 1: {
                    int idx = rnd.nextInt(model.size() + 1);
                    l.add(idx, value);
                    model.add(idx, value);
                    break;
                }
                case 2:
                    if (!model.isEmpty()) {
                        int idx = rnd.nextInt(model.size());
                        assertEquals(model.remove(idx).intValue(), l.remove(idx));
                    }
                    break;
                case 3:
                    if (!model.isEmpty()) {
                        int idx = rnd.nextInt(model.size());
                        assertEquals(model.get(idx).intValue(), l.get(idx));
                    }
                    break;
                default:
                    assertEquals(model.contains(value), l.contains(value));
            }
            assertEquals(model.size(), l.size());
        }
        assertSameContent(model, l);
    }

    // ---------------------------------------------------------------- counters must be honest

    @Test
    void containsCountsEveryCell() {
        IList l = create();
        for (int i = 0; i < 100; i++) {
            l.add(i);
        }
        l.metrics().reset();
        assertFalse(l.contains(-1));    // absent: the whole structure is scanned
        assertEquals(100, l.metrics().steps);
        assertEquals(100, l.metrics().comparisons);
    }

    @Test
    void middleInsertCountsMovesInBothStructures() {
        IList l = create();
        for (int i = 0; i < 100; i++) {
            l.add(i);
        }
        l.metrics().reset();
        l.add(50, 999);
        assertTrue(l.metrics().moves > 0, "a middle insert must never report 0 moves (shifts or link updates)");
    }
}
