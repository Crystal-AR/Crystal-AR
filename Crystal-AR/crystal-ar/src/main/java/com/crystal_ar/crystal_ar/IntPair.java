package com.crystal_ar.crystal_ar;

/*
 * Public class: IntPair.
 * Basic class that collects a word and its position.
 * Properties: x, y.
 * Methods: equals, hashCode, compareTo.
 * @params int - first int in the pair.
 * @params int - second int in the pair.
 * @return n/a.
 */
public class IntPair implements Comparable<IntPair> {
    /*
     * Properties.
     */
    public int x; // First int.
    public int y; // Second int.

    /* Constructor.
     * @params int - first int in the pair.
     * @params int - second int in the pair.
     * @return n/a.
     */
    public IntPair(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    /*
     * Checks whether an object is equal to the IntPair.
     * @params Object - object to check equals against.
     * @return boolean - whether the given object is equal to the IntPair.
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof IntPair) && ((IntPair) o).x == this.x && ((IntPair) o).y == this.y;
    }

    /*
     * Computes the hashcode for the IntPair.
     * @params n/a.
     * @return int - hashcode for the IntPair.
     */
    @Override
    public int hashCode() {
        return this.x * 10000000 + this.y;
    }

    /*
     * Compares a given IntPair against itself.
     * @params IntPair - to be compared against.
     * @return int - representing the comparison between the IntPairs.
     */
    @Override
    public int compareTo(IntPair other) {
        return (1000000 * x + y) - (1000000 * other.x + other.y);
    }
}
