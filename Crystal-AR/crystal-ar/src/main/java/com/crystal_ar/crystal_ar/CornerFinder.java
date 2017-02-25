package com.crystal_ar.crystal_ar;

import android.graphics.Bitmap;

import java.util.Set;
import java.util.TreeMap;

/*
 * Public class: CornerFinder.
 * Class for finding corners in an image.
 * Properties: paint_threshold, corner_threshold.
 * Methods: findCorners.
 * @params Bitmap - Image to find corners in.
 * @params int (optional) - x-coordinate of the center of the table.
 * @params int (optional) - y-coordinate of the center of the table.
 * @params int (optional, if two first int parameters) - paint threshold.
 * @params int (optional, unless third int parameter) - corner threshold.
 * @return n/a.
 */
public class CornerFinder {
    /*
     * Private variables.
     */
    private int[] intense = new int[1];
    private int[][] magic = new int[1][1];
    private CrystalCustomQueue pixels_to_look_at = new CrystalCustomQueue();

    /*
     * Properties.
     */
    public int paint_threshold = 5; // Paint threshold.
    public int corner_threshold = 20; // Corner threshold.

    /*
     * Finds corners in an image assuming that the center of the table is in the middle of the
     * image.
     * @params Bitmap - image to find corners in.
     * @return IntPair[] - list of corner coordinates.
     */
    public IntPair[] findCorners(Bitmap img) throws NullPointerException, IllegalArgumentException, RuntimeException {
        if (img == null)
            throw new NullPointerException("findCorners(img) given null for image");
        return findCorners(img, img.getWidth()/2, img.getHeight()/2);
    }

    /*
     * Finds corners in an image that passes certain thresholds and where the center of the table is
     * at the given position.
     * @params Bitmap - image to find corners in.
     * @params int - x-coordinate of the center of the table.
     * @params int - y-coordinate of the center of the table.
     * @params int - paint threshold.
     * @params int - corner threshold.
     * @return IntPair[] - list of corner coordinates.
     */
    public IntPair[] findCorners(Bitmap img, int center_x, int center_y, int pt, int ct) throws NullPointerException, IllegalArgumentException, RuntimeException {
        paint_threshold = pt;
        corner_threshold = ct;
        return findCorners(img, center_x, center_y);
    }

    /*
     * Finds corners in an image where the center of the table is at the given position.
     * @params Bitmap - image to find corners in.
     * @params int - x-coordinate of the center of the table.
     * @params int - y-coordinate of the center of the table.
     * @return IntPair[] - list of corner coordinates.
     */
    public IntPair[] findCorners(Bitmap img, int center_x, int center_y) throws NullPointerException, IllegalArgumentException, RuntimeException {
        if (img == null)
            throw new NullPointerException("findCorners(img, x, y) given null for image");
        int w = img.getWidth();
        int h = img.getHeight();
        if (w == 0 || h == 0)
            throw new IllegalArgumentException("findCorners(img, x, y) given empty image");

        // compute a 2d array of intensities
        if (intense.length != w * h)
            intense = new int[w * h];
        /*
         * 0      intensities
         * 1      looked at
         * 2      table (painted)
         * 3      (bad) edge
         * 4      looked at
         * 5      (good) edge
         */
        if (magic.length != w || magic[0].length != h)
            magic = new int[w][h];
        img.getPixels(intense, 0, w, 0, 0, w, h);

        // "paint" the table
        pixels_to_look_at.clear();
        pixels_to_look_at.enqueue(center_x, center_y);
        ++magic[w/2][h/2];
        int threshold = this.paint_threshold;
        while (!pixels_to_look_at.is_empty()) {
            int pixel = pixels_to_look_at.dequeue();
            int pixel_x = pixel / 100000;
            int pixel_y = pixel % 100000;
            if (pixel_x != 0) {

                if (computePixelDiff(intense[(pixel_x-1) + pixel_y * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x-1][pixel_y] < 1) {
                        ++magic[pixel_x-1][pixel_y];
                        pixels_to_look_at.enqueue(pixel_x-1, pixel_y);
                    }
                }
            }
            if (pixel_y != 0) {
                if (computePixelDiff(intense[pixel_x + (pixel_y-1) * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x][pixel_y-1] < 1) {
                        ++magic[pixel_x][pixel_y-1];
                        pixels_to_look_at.enqueue(pixel_x, pixel_y-1);
                    }
                }
            }
            if (pixel_x != w - 1) {
                if (computePixelDiff(intense[(pixel_x+1) + pixel_y * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x+1][pixel_y] < 1) {
                        ++magic[pixel_x+1][pixel_y];
                        pixels_to_look_at.enqueue(pixel_x+1, pixel_y);
                    }
                }
            }
            if (pixel_y != h - 1) {
                if (computePixelDiff(intense[pixel_x + (pixel_y+1) * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x][pixel_y+1] < 1) {
                        ++magic[pixel_x][pixel_y+1];
                        pixels_to_look_at.enqueue(pixel_x, pixel_y+1);
                    }
                }
            }
            magic[pixel_x][pixel_y] = 2;
        }



        // find the edges (and lots of noise in the table)
        IntPair farthest_point = new IntPair(w/2, h/2);
        double max_dist = 0;
        for (int x = 1; x < w - 1; ++x) {
            for (int y = 1; y < h - 1; ++y) {
                if (magic[x][y] < 1) {
                    if ((2 <= magic[x+1][y] && magic[x+1][y] < 3) || (2 <= magic[x-1][y] && magic[x-1][y] < 3) || (2 <= magic[x][y-1] && magic[x][y-1] < 3) || (2 <= magic[x][y+1] && magic[x][y+1] < 3)) {
                        magic[x][y] = 3;
                        double dist = (x-center_x)*(x-center_x) + (y-center_y)*(y-center_y);
                        if (dist > max_dist) {
                            max_dist = dist;
                            farthest_point.x = x;
                            farthest_point.y = y;
                        }
                    }
                }
            }
        }

        // eliminate the noise in the middle of the table
        pixels_to_look_at.clear();
        CrystalCustomQueue border_pixels = new CrystalCustomQueue();
        pixels_to_look_at.enqueue(farthest_point.x, farthest_point.y);
        while (!pixels_to_look_at.is_empty()) {
            int pixel = pixels_to_look_at.dequeue();
            int x = pixel / 100000;
            int y = pixel % 100000;
            magic[x][y] = 5;
            border_pixels.enqueue(x, y);
            if (x != 0 && magic[x - 1][y] == 3) {
                pixels_to_look_at.enqueue(x-1, y);
                magic[x-1][y] = 4;
            }
            if (x != magic.length-1 && magic[x + 1][y] == 3) {
                pixels_to_look_at.enqueue(x+1, y);
                magic[x+1][y] = 4;
            }
            if (y != 0 && magic[x][y - 1] == 3) {
                pixels_to_look_at.enqueue(x, y-1);
                magic[x][y-1] = 4;
            }
            if (y != magic[0].length-1 && magic[x][y + 1] == 3) {
                pixels_to_look_at.enqueue(x, y+1);
                magic[x][y+1] = 4;
            }
            if (x != 0 && y != 0 && magic[x - 1][y - 1] == 3) {
                pixels_to_look_at.enqueue(x-1, y-1);
                magic[x-1][y-1] = 4;
            }
            if (x != magic.length-1 && y != 0 && magic[x + 1][y - 1] == 3) {
                pixels_to_look_at.enqueue(x+1, y-1);
                magic[x+1][y-1] = 4;
            }
            if (x != 0 && y != magic[0].length-1 && magic[x - 1][y + 1] == 3) {
                pixels_to_look_at.enqueue(x-1, y+1);
                magic[x-1][y+1] = 4;
            }
            if (x != magic.length-1 && y != magic[0].length-1 && magic[x + 1][y + 1] == 3) {
                pixels_to_look_at.enqueue(x+1, y+1);
                magic[x+1][y+1] = 4;
            }
        }



        // find the most extreme pixels over various rotations of the coordinate actions
        TreeMap<IntPair, Integer> extrema = new TreeMap<IntPair, Integer>();
        for (double theta = 0; theta < Math.PI/2; theta += 5 * Math.PI/180) {
            IntPair[] set = findExtrema(border_pixels, theta);
            for (int i = 0; i < set.length; ++i) {
                if (extrema.containsKey(set[i])) {
                    if (!extrema.containsKey(set[i])) throw new RuntimeException("key '" + set[i] + ", " + set[i] + "' not found in extrema-finding loop");
                    extrema.put(set[i], extrema.get(set[i]) + 1);
                }
                else
                    extrema.put(set[i], 1);
            }
        }



        // condense these extrema into 4 (likely) corners
        for (IntPair key : extrema.keySet()) {
            for (IntPair key2 : extrema.keySet()) {
                int dist = (key.x-key2.x)*(key.x-key2.x) + (key.y-key2.y)*(key.y-key2.y);
                if (key == key2)
                    continue;
                if (dist < this.corner_threshold) {
                    if (!extrema.containsKey(key)) throw new RuntimeException("key '" + key.x + ", " + key.y + "' not found in condense loop");
                    if (!extrema.containsKey(key2)) throw new RuntimeException("key '" + key.x + ", " + key.y + "' not found in condense loop");
                    int count1 = extrema.get(key);
                    int count2 = extrema.get(key2);
                    if (count1 > count2) {
                        extrema.put(key, count1+count2);
                        extrema.put(key2, 0);
                    }
                    else {
                        extrema.put(key, 0);
                        extrema.put(key2, count1+count2);
                    }
                }
            }
        }


        // create set to return, and return it
        IntPair[] corners = new IntPair[4];
        for (int i = 0; i < 4; ++i) {
            IntPair corner = findKeyWithLargestValue(extrema);
            corners[i] = corner;
            if (corner == null) throw new RuntimeException("Only found " + i + " corners");
            if (!extrema.containsKey(corner)) throw new RuntimeException("The 'found' corner is not in the extrema dictionary");
            extrema.remove(corner);
        }

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 3; ++j) {
                double first = Math.atan2(corners[j].y-center_y, corners[j].x-center_x);
                double second = Math.atan2(corners[j+1].y-center_y, corners[j+1].x-center_x);
                if (first > second) {
                    IntPair temp = corners[j];
                    corners[j] = corners[j+1];
                    corners[j+1] = temp;
                }
            }
        }

        return corners;
    }

    /*
     * a method to help debuging by printing out samples of the table in a grid
     */

    /*
     * Prints magic.
     * at the given position.
     * @params int[][] - magic.
     * @params int - step.
     * @return void.
     */
    private void printMagic(int[][] magic, int step) {
        String str = "";
        System.out.println(magic.length + " : " + step);
        for (int y = 0; y < magic[0].length; y += step) {
            for (int x = 0; x < magic.length; x += step/2) {
                str += (int) (magic[x][y]/1000);
            }
            str += "\n";
        } 
        System.out.println(str);
    }

    /*
     * This function takes a list of border pixels and a theta.
     * It then rotates the coordinate axes by theta, and finds the points with the smallest and
     * largest points in terms of their (rotated) x- and y- coordinates.
     * @params CrystalCustomQueue - 2d array in which 2 is a "point" in the set.
     * @params double - angle of the "x-axis".
     * @return IntPair[] - 4 extrema.
     */
    private IntPair[] findExtrema(CrystalCustomQueue borders, double theta) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double adj;
        double opp;

        double min_adj = 1e300;
        IntPair min_adj_pt = new IntPair(0, 0);
        double max_adj = -1e300;
        IntPair max_adj_pt = new IntPair(0, 0);
        double min_opp = 1e300;
        IntPair min_opp_pt = new IntPair(0, 0);
        double max_opp = -1e300;
        IntPair max_opp_pt = new IntPair(0, 0);

        for (int i = 0; i < borders.size(); ++i) {
            int pixel = borders.get(i);
            int x = pixel / 100000;
            int y = pixel % 100000;
            adj = cos * x + sin * y;
            opp = -1 * sin * x + cos * y;

            if (adj > max_adj) {
                max_adj = adj;
                max_adj_pt.x = x;
                max_adj_pt.y = y;
            }
            if (adj < min_adj) {
                min_adj = adj;
                min_adj_pt.x = x;
                min_adj_pt.y = y;
            }
            if (opp > max_opp) {
                max_opp = opp;
                max_opp_pt.x = x;
                max_opp_pt.y = y;
            }
            if (opp < min_opp) {
                min_opp = opp;
                min_opp_pt.x = x;
                min_opp_pt.y = y;
            }
        }

        IntPair[] rtn = new IntPair[4];
        rtn[0] = min_adj_pt; // smallest "x"
        rtn[1] = min_opp_pt; // smallest "y"
        rtn[2] = max_adj_pt; // largest "x"
        rtn[3] = max_opp_pt; // largest "y"
        return rtn;
    }

    /*
     * Finds the key in a dictionary (map) with the largest value.
     * @params TreeMap - the dictionary to search.
     * @return IntPair - the key whose value is the maximum.
     */
    private IntPair findKeyWithLargestValue(TreeMap<IntPair, Integer> dict) {
        Set<IntPair> keys = dict.keySet();
        int max_val = -100;
        IntPair max_key = null;
        for (IntPair key : keys) {
            if (!dict.containsKey(key)) throw new RuntimeException("findKeyWithLargestValue given no keys");
            int val = dict.get(key);
            if (val > max_val) {
                max_val = val;
                max_key = key;
            }
        }
        return max_key;
    }

    /*
     * Computes the pixel difference.
     * @params int - first pixel.
     * @params int - second pixel.
     * @return int - pixel difference.
     */
    private int computePixelDiff(int x, int y) {
        return Math.abs((0xFF000000 & x) - (0xFF000000 & y)) / 16777216 + Math.abs((0x00FF0000 & x) - (0x00FF0000 & y)) / 65536 + Math.abs((0x0000FF00 & x) - (0x0000FF00 & y)) / 256;
    }
}

