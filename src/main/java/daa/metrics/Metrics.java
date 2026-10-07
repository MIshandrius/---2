package daa.metrics;

/**
 * Counters for the physical operations of a data structure.
 * They are increased INSIDE the methods of the structures (never estimated afterwards).
 *
 * steps       - one read of an array cell OR one visit of a list node
 * moves       - one element shifted in an array OR one link (pointer) updated in a list
 *               (in the heap: one swap or one copy of an element)
 * comparisons - one comparison of two elements
 */
public class Metrics {
    public long steps = 0;
    public long moves = 0;
    public long comparisons = 0;

    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }
}
