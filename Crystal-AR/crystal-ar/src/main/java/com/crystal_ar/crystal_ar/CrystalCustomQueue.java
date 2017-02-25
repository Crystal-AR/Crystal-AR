package com.crystal_ar.crystal_ar;

/*
 * Public class: CrystalCustomQueue.
 * Custom queue made for CornerFinder.
 * Properties: n/a.
 * Methods: isEmpty, size, get, clear, resize, enqueue, dequeue.
 * @params n/a.
 * @return n/a.
 */
public class CrystalCustomQueue {

    /*
     * Private variables.
     */
    private int[] x_arr;
    private int total, first, next;

    /* Constructor.
     * @params n/a.
     * @return n/a.
     */
    public CrystalCustomQueue()
    {
        x_arr = new int[2];
    }

    /*
     * Checks whether the queue is empty.
     * @params n/a.
     * @return boolean - whether the queue is empty or not.
     */
    public boolean isEmpty() {
        return total == 0;
    }

    /*
     * Returns the size of the queue.
     * @params n/a.
     * @return int - the size of the queue.
     */
    public int size() {
        return total;
    }

    /*
     * Gets int in the queue at the given index.
     * @params int - index of the queue.
     * @return int - the int at the given index in the queue.
     */
    public int get(int index) {
        return x_arr[(first + index) % x_arr.length];
    }

    /*
     * Clears the queue.
     * @params n/a.
     * @return void.
     */
    public void clear() {
        first = 0;
        next = 0;
        total = 0;
    }

    /*
     * Resizes the que to a given capacity.
     * @params int - the new capacity of the queue.
     * @return void.
     */
    private void resize(int capacity) {
        if (capacity < x_arr.length) return;
        int[] x_tmp = new int[capacity];

        for (int i = 0; i < total; i++) {
            x_tmp[i] = x_arr[(first + i) % x_arr.length];
        }

        x_arr = x_tmp;
        first = 0;
        next = total;
    }

    /*
     * Enqueues a pair of x and y coordinates.
     * @params int - x to be enqueued.
     * @params int - y to be enqueued.
     * @return int - size of the queue.
     */
    public int enqueue(int x, int y) {
        if (x_arr.length == total) {
            resize((int) (x_arr.length * 1.5));
        }
        x_arr[next++] = 100000*x+y;

        if (next == x_arr.length) next = 0;
        total++;
        return total;
    }

    /*
     * Dequeues an item.
     * @params n/a.
     * @return int - a pair of x, y coordinates.
     */
    public int dequeue() {
        if (total == 0) throw new java.util.NoSuchElementException();

        int pop = x_arr[first];
        if (++first == x_arr.length) first = 0;

        if (--total > 0 && total == x_arr.length / 4) {
            resize(x_arr.length / 2);
        }
        return pop;
    }
}
