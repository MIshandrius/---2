package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import java.util.PriorityQueue;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MinHeapTest {

    // ---------------------------------------------------------------- edge cases

    @Test
    void emptyHeapThrowsIllegalState() {
        MinHeap h = new MinHeap();
        assertThrows(IllegalStateException.class, () -> h.peekMin());
        assertThrows(IllegalStateException.class, () -> h.extractMin());
    }

    @Test
    void singleElement() {
        MinHeap h = new MinHeap();
        h.insert(7);
        assertEquals(7, h.peekMin());
        assertEquals(1, h.size());
        assertEquals(7, h.extractMin());
        assertEquals(0, h.size());
        assertThrows(IllegalStateException.class, () -> h.extractMin());
    }

    @Test
    void duplicatesAreKept() {
        MinHeap h = new MinHeap();
        for (int i = 0; i < 10; i++) {
            h.insert(5);
        }
        h.insert(1);
        assertEquals(1, h.extractMin());
        for (int i = 0; i < 10; i++) {
            assertEquals(5, h.extractMin());
        }
        assertEquals(0, h.size());
    }

    @Test
    void peekMinDoesNotRemoveAndCostsOneStep() {
        MinHeap h = new MinHeap();
        for (int i = 100; i > 0; i--) {
            h.insert(i);
        }
        h.metrics().reset();
        assertEquals(1, h.peekMin());
        assertEquals(1, h.metrics().steps);
        assertEquals(0, h.metrics().comparisons);
        assertEquals(100, h.size());
    }

    // ---------------------------------------------------------------- heap property

    @Test
    void heapPropertyHoldsAfterEveryInsertAndExtract() {
        Random rnd = new Random(7);
        MinHeap h = new MinHeap();
        PriorityQueue<Integer> model = new PriorityQueue<>();      // expected result (tests only)
        for (int step = 0; step < 5000; step++) {
            if (model.isEmpty() || rnd.nextInt(3) != 0) {
                int v = rnd.nextInt(1000) - 500;
                h.insert(v);
                model.add(v);
            } else {
                assertEquals(model.poll().intValue(), h.extractMin());
            }
            assertTrue(h.isValidHeap(), "a[parent] <= a[child] violated at step " + step);
            assertEquals(model.size(), h.size());
            if (!model.isEmpty()) {
                assertEquals(model.peek().intValue(), h.peekMin());
            }
        }
    }

    // ---------------------------------------------------------------- sorted output

    @Test
    void nExtractsReturnNonDecreasingOrder() {
        Random rnd = new Random(42);
        int n = 20_000;
        MinHeap h = new MinHeap();
        for (int i = 0; i < n; i++) {
            h.insert(rnd.nextInt());
        }
        int prev = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int v = h.extractMin();
            assertTrue(v >= prev);
            prev = v;
        }
        assertEquals(0, h.size());
    }

    @Test
    void ascendingInputIsBestCaseForInsert() {
        MinHeap asc = new MinHeap();
        MinHeap desc = new MinHeap();
        for (int i = 0; i < 1000; i++) {
            asc.insert(i);
            desc.insert(1000 - i);
        }
        assertTrue(asc.isValidHeap());
        assertTrue(desc.isValidHeap());
        assertEquals(999, asc.metrics().comparisons);      // exactly one comparison per insert (except the first)
        assertTrue(desc.metrics().comparisons > asc.metrics().comparisons);
    }

    // ---------------------------------------------------------------- bonus B: buildHeap

    @Test
    void buildHeapMakesValidHeapAndSortedOutput() {
        Random rnd = new Random(3);
        int[] sizes = {0, 1, 2, 3, 10, 1000, 5001};
        for (int s = 0; s < sizes.length; s++) {
            int n = sizes[s];
            int[] data = new int[n];
            for (int i = 0; i < n; i++) {
                data[i] = rnd.nextInt(100);
            }
            MinHeap h = new MinHeap();
            h.buildHeap(data);
            assertTrue(h.isValidHeap(), "n = " + n);
            assertEquals(n, h.size());
            int prev = Integer.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                int v = h.extractMin();
                assertTrue(v >= prev);
                prev = v;
            }
        }
    }

    @Test
    void buildHeapUsesAtMostTwoNComparisons() {
        int n = 100_000;
        int[] data = new int[n];
        for (int i = 0; i < n; i++) {
            data[i] = n - i;                    // descending: worst case for n separate inserts
        }
        MinHeap built = new MinHeap();
        built.buildHeap(data);
        MinHeap inserted = new MinHeap();
        for (int i = 0; i < n; i++) {
            inserted.insert(data[i]);
        }
        assertTrue(built.metrics().comparisons <= 2L * n, "Floyd: at most 2n comparisons");
        assertTrue(inserted.metrics().comparisons > 5L * built.metrics().comparisons);
    }
}
