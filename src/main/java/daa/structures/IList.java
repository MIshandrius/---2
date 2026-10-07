package daa.structures;

import daa.metrics.Metrics;

/** Same methods for DynamicArray and MyLinkedList, so ONE benchmark can run both. */
public interface IList {

    void add(int x);
    void add(int index, int x);
    int remove(int index);
    int get(int index);
    boolean contains(int x);
    int size();
    Metrics metrics();
}
