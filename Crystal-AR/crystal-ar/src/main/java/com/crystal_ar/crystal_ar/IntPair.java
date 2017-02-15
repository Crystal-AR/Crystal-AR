package com.crystal_ar.crystal_ar;

/**
 * Created by Sabastian on 2/14/17.
 */

public class IntPair implements Comparable<IntPair> {
    public int x;
    public int y;
    public IntPair(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IntPair) && ((IntPair) o).x == this.x && ((IntPair) o).y == this.y;
    }

    @Override
    public int hashCode() {
        return this.x * 10000000 + this.y;
    }

    @Override
    public int compareTo(IntPair other) {
        return (1000000 * x + y) - (1000000 * other.x + other.y);
    }
}
