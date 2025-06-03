package me.datatags.infinitodewheelsolver;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * The purpose of this class is to have a collection that keeps only the N items with the highest score.
 * It also lets you iterate on in order of priority highest to lowest.
 *
 * @param <T> The type of item to store in the collection
 */
public class BoundedPriorityQueue<T extends Comparable<T>> implements Iterable<T> {
    private final PriorityQueue<T> queue;
    private final int maxSize;

    public BoundedPriorityQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new PriorityQueue<>(maxSize);
    }

    public void add(T item) {
        if (queue.size() < maxSize) {
            queue.add(item);
            return;
        }

        T lowest = queue.peek();
        if (item.compareTo(lowest) > 0) {
            queue.poll(); // Remove lowest
            queue.add(item);
        }
        // Else discard the new item
    }

    public void clear() {
        queue.clear();
    }

    public PriorityQueue<T> getQueue() {
        return queue;
    }

    public int size() {
        return queue.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return Arrays.stream(queue.toArray()).map(o -> (T) o).sorted(Comparator.reverseOrder()).iterator();
    }
}

