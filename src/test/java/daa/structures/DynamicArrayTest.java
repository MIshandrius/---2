package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DynamicArrayTest extends IListContractTest {

    @Override
    IList create() {
        return new DynamicArray();
    }

    @Test
    void getCostsExactlyOneStep() {
        IList a = new DynamicArray();
        for (int i = 0; i < 1000; i++) {
            a.add(i);
        }
        a.metrics().reset();
        a.get(0);
        a.get(999);
        a.get(500);
        assertEquals(3, a.metrics().steps, "get(i) is one array read for every i");
    }

    @Test
    void insertShiftsExactlyTheElementsAfterTheIndex() {
        IList a = new DynamicArray();
        for (int i = 0; i < 1000; i++) {
            a.add(i);                   // capacity is now 1280, so add(index, x) does not grow
        }
        a.metrics().reset();
        a.add(100, -5);                 // elements 100..999 move: 900 shifts
        assertEquals(900, a.metrics().moves);
        a.metrics().reset();
        a.remove(100);                  // elements 101..1000 move back: 900 shifts
        assertEquals(900, a.metrics().moves);
    }

    @Test
    void capacityDoublesAndGrowCopiesAreCounted() {
        IList a = new DynamicArray();   // initial capacity 10
        for (int i = 0; i < 10; i++) {
            a.add(i);
        }
        a.metrics().reset();
        a.add(10);                      // full -> grow 10 -> 20, copies 10 elements
        assertEquals(10, a.metrics().moves);
        a.metrics().reset();
        for (int i = 11; i < 20; i++) {
            a.add(i);                   // fits into the doubled array
        }
        assertEquals(0, a.metrics().moves);
    }

    @Test
    void amortizedAppendCostIsConstant() {
        IList a = new DynamicArray();
        int n = 100_000;
        for (int i = 0; i < n; i++) {
            a.add(i);
        }
        assertTrue(a.metrics().moves < 2L * n, "2x growth copies fewer than 2n elements in total");
    }
}
