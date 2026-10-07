package daa.structures;

import daa.metrics.Metrics;

public final class DynamicArray implements IList {
    private int[] arr;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        arr = new int[10];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    @Override
    public int get(int index){
        checkIndex(index);
        metrics.steps++;
        return arr[index];
    }

    private void checkIndex(int index){
        if (index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public void add(int x){
        if (size == arr.length) grow();
        arr[size++] = x;
    }

    private void grow() {
        int[] bigger = new int[arr.length * 2];      // 2x growth
        for(int i = 0; i < size; i++){
            bigger[i] = arr[i];
            metrics.moves++;
        }
        arr = bigger;
    }

    @Override
    public void add(int index, int x){
        if (index < 0 || index > size){
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (size == arr.length){
            grow();
        }

        // shift arr[index..size-1] one place to the RIGHT, starting from the end
        for(int i = size; i > index; i--){
            arr[i] = arr[i - 1];
            metrics.moves++;
        }
        arr[index] = x;
        size++;
    }

    @Override
    public int remove(int index){
        checkIndex(index);
        metrics.steps++;
        int removed = arr[index];
        // shift arr[index+1..size-1] one place to the LEFT
        for (int i = index; i < size - 1; i++){
            arr[i] = arr[i + 1];
            metrics.moves++;
        }
        size--;
        return removed;
    }

    @Override
    public boolean contains(int x){
        for (int i = 0; i < size; i++){
            metrics.steps++;
            metrics.comparisons++;
            if(arr[i] == x){
                return true;
            }
        }
        return false;
    }
}
