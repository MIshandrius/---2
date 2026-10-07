package daa.structures;

import daa.metrics.Metrics;

/**
 * Array-based binary min-heap of int.
 * Node i has parent (i - 1) / 2 and children 2i + 1 and 2i + 2.
 * Counters: steps = array reads, comparisons = element comparisons,
 * moves = swaps and copies (one swap counts as one move).
 */
public final class MinHeap {
    private int[] arr = new int[16];
    private int size;
    private final Metrics metrics = new Metrics();

    public int size(){
        return size;
    }

    private void swap(int i, int j){
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public Metrics metrics(){
        return metrics;
    }

    private void grow(){
        int[] bigger = new int[arr.length * 2];
        for (int i = 0; i < size; i++){
            bigger[i] = arr[i];
            metrics.moves++;
        }
        arr = bigger;
    }

    /** Bubble-up. */
    public void insert(int x){
        if (size == arr.length){
            grow();
        }
        arr[size] = x;
        int i = size;
        size = size + 1;

        while (i > 0){
            int parent = (i - 1) / 2;
            metrics.steps++;
            metrics.comparisons++;
            if (x >= arr[parent]){
                break;
            }
            swap(i, parent);
            metrics.moves++;
            i = parent;
        }
    }

    public int peekMin(){
        if(size == 0){
            throw new IllegalStateException("heap is empty");
        }
        metrics.steps++;
        return arr[0];
    }

    /** Takes the last element to the root and bubbles it down. */
    public int extractMin(){
        if(size == 0){
            throw new IllegalStateException("heap is empty");
        }
        metrics.steps++;
        int min = arr[0];
        metrics.steps++;
        arr[0] = arr[size - 1];
        metrics.moves++;
        size = size - 1;

        siftDown(0);
        return min;
    }

    /** Bubble-down of the element at position start. */
    private void siftDown(int start){
        int i = start;
        while (true){
            int left = 2 * i + 1;
            int right =  2 * i + 2;
            if (left >= size){
                break;
            }

            int smaller = left;
            metrics.steps++;
            if (right < size){
                metrics.steps++;
                metrics.comparisons++;
                if(arr[right] < arr[left]){
                    smaller = right;
                }
            }

            metrics.comparisons++;

            if (arr[i] <= arr[smaller]){
                break;
            }
            swap(i, smaller);
            metrics.moves++;
            i = smaller;
        }
    }

    /** Bonus B: Floyd's bottom-up construction, O(n). Replaces the current content. */
    public void buildHeap(int[] src){
        arr = new int[Math.max(src.length, 1)];
        for (int i = 0; i < src.length; i++){
            arr[i] = src[i];
        }
        size = src.length;

        for (int i = size / 2 - 1; i >= 0; i--){
            siftDown(i);
        }
    }

    /** For the tests: a[parent] <= a[child] for every node. Does not change the counters. */
    public boolean isValidHeap(){
        for (int i = 1; i < size; i++){
            if (arr[(i - 1) / 2] > arr[i]){
                return false;
            }
        }
        return true;
    }
}
