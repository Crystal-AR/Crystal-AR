package com.crystal_ar.crystal_ar;

/**
 * Created by Sabastian on 2/14/17.
 */

public class CrystalCustomQueue {

    private int[] x_arr;

    private int total, first, next;

    public CrystalCustomQueue()
    {
        x_arr = new int[2];
    }

    public boolean is_empty() {
        return total == 0;
    }

    private void resize(int capacity)
    {
        int[] x_tmp = new int[capacity];

        for (int i = 0; i < total; i++) {
            x_tmp[i] = x_arr[(first + i) % x_arr.length];
        }

        x_arr = x_tmp;
        first = 0;
        next = total;
    }

    //enqueue a pair of x and y coodinates
    public int enqueue(int x, int y)
    {
        if (x_arr.length == total) {
            resize(x_arr.length * 2);

        }
        x_arr[next++] = 100000*x+y;

        if (next == x_arr.length) next = 0;
        total++;
        return total;
    }

    //returns a pair of x,y coodinates
    public int dequeue()
    {
        if (total == 0) throw new java.util.NoSuchElementException();

        int pop = x_arr[first];
//        x_arr[first] = null;
//        y_arr[first] = null;

        if (++first == x_arr.length) first = 0;

        if (--total > 0 && total == x_arr.length / 4) {
            resize(x_arr.length / 2);
        }
        return pop;
    }

}
