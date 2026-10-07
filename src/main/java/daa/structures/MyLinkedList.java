package daa.structures;

import daa.metrics.Metrics;

public final class MyLinkedList implements IList {
    private static class Node{
        int value;
        Node next;
        Node(int value){
            this.value = value;
        }
    }
    private Node head, tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public int size(){
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    /** Walks from head to the node at position index: index + 1 node visits. */
    private Node nodeAt(int index){
        Node cur = head;
        metrics.steps++;
        for(int k = 0; k < index; k++){
            cur = cur.next;
            metrics.steps++;
        }
        return cur;
    }

    @Override
    public void add(int x){
        Node n = new Node(x);
        if (tail == null){
            head = n;
        }
        else{
            tail.next = n;
        }
        tail = n;
        size++;
        metrics.moves += 2;      // two links updated: (head or tail.next) and tail
    }

    @Override
    public void add(int index, int x){
        if (index < 0 || index > size){
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == size){
            add(x);
            return;
        }
        Node n = new Node(x);

        if (index == 0){
            n.next = head;
            head  = n;
            metrics.moves += 2;
        }
        else{
            Node prev = nodeAt(index - 1);
            n.next = prev.next;
            prev.next = n;
            metrics.moves += 2;
        }
        size++;
    }

    @Override
    public int remove(int index){
        checkIndex(index);
        Node removed;
        if (index == 0){
            removed = head;
            metrics.steps++;
            head = head.next;
            metrics.moves++;
            if (head == null){
                tail = null;
                metrics.moves++;
            }
        }
        else{
            Node prev = nodeAt(index - 1);
            removed = prev.next;
            metrics.steps++;
            prev.next = removed.next;
            metrics.moves++;
            if (removed == tail){
                tail = prev;
                metrics.moves++;
            }
        }
        size--;
        return removed.value;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    private void checkIndex(int index){
        if (index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public boolean contains(int x){
        Node cur = head;
        while (cur != null){
            metrics.steps++;
            metrics.comparisons++;
            if(cur.value == x){
                return true;
            }
            cur = cur.next;
        }
        return false;
    }
}
