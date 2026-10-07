package daa.structures;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MyLinkedListTest extends IListContractTest {

    @Override
    IList create() {
        return new MyLinkedList();
    }

    @Test
    void getWalksIndexPlusOneNodes() {
        IList l = new MyLinkedList();
        for (int i = 0; i < 1000; i++) {
            l.add(i);
        }
        l.metrics().reset();
        l.get(0);
        assertEquals(1, l.metrics().steps);
        l.metrics().reset();
        l.get(999);
        assertEquals(1000, l.metrics().steps, "pointer chasing: the cost grows with the index");
    }

    @Test
    void headInsertAndRemoveDoNotWalk() {
        IList l = new MyLinkedList();
        for (int i = 0; i < 1000; i++) {
            l.add(i);
        }
        l.metrics().reset();
        l.add(0, -1);
        assertEquals(0, l.metrics().steps);
        assertEquals(2, l.metrics().moves);
        l.metrics().reset();
        l.remove(0);
        assertEquals(1, l.metrics().steps);
        assertEquals(1, l.metrics().moves);
    }

    @Test
    void middleInsertWalksToThePredecessorAndUpdatesTwoLinks() {
        IList l = new MyLinkedList();
        for (int i = 0; i < 1000; i++) {
            l.add(i);
        }
        l.metrics().reset();
        l.add(500, -1);
        assertEquals(500, l.metrics().steps);       // nodes 0..499
        assertEquals(2, l.metrics().moves);         // no shifting, only 2 links
    }

    @Test
    void appendAfterEmptyingUsesCorrectHeadAndTail() {
        IList l = new MyLinkedList();
        l.add(1);
        l.add(2);
        l.remove(1);                    // removes the tail
        l.remove(0);                    // removes the last node
        l.add(9);
        l.add(8);
        assertEquals(9, l.get(0));
        assertEquals(8, l.get(1));
        assertEquals(2, l.size());
    }
}
