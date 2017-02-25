package com.crystal_ar.crystal_ar;

import android.graphics.Rect;

/*
 * Public class: Word.
 * Basic class that collects a word and its position.
 * Properties: str, x, y, width, height.
 * Methods: toString.
 * @params String - word.
 * @params Rect - rectangle surrounding the word.
 * @return n/a.
 */
public class Word {

    /*
     * Constructor.
     * @params String - word.
     * @params Rect - rectangle surrounding the word.
     * @return n/a.
     */
    public Word(String s, Rect rect) {
        str = s;
        x = rect.left;
        y = rect.top;
        width = rect.width();
        height = rect.height();
    }

    /*
     * Properties.
     */
    public String str; // String that is the word.
    public int x; // X-coordinate of the top left corner of the rectangle surrounding the word.
    public int y; // Y-coordinate of the top left corner of the rectangle surrounding the word.
    public int width; // Width of the rectangle surrounding the word.
    public int height; // Height of the rectangle surrounding the word.

    /*
     * Returns a string representation of the entire Word object.
     * @params n/a.
     * @return String - string representation of the Word.
     */
    @Override
    public String toString() {
        return String.valueOf(str) + " (" + String.valueOf(x) + ", " + String.valueOf(y) + ") (" + String.valueOf(width) + ", " + String.valueOf(height) + ")";
    }
}
