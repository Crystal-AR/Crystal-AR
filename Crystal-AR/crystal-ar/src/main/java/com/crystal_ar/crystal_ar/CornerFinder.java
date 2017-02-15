package com.crystal_ar.crystal_ar;

import android.graphics.Bitmap;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Sabastian on 2/14/17.
 */

public class CornerFinder {

    private void print_magic(int[][] magic, int step) {
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
  * This function takes a 2d array representing a set of points.
  * It then rotates the coordinate axes by theta, and finds the points with the smallest and
  * largest points in terms of their (rotated) x- and y- coordiantes
  *
  * @param borders - 2d array in which 2 is a "point" in the set
  * @param theta - angle of the "x-axis"
  * @return array of the 4 extrema
  */
    private IntPair[] find_extrema(int[][] magic, double theta) {
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

        for (int x = 0; x < magic.length; ++x) {
            for (int y = 0; y < magic[x].length; ++y) {
                if (magic[x][y] < 5000)
                    continue;

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
        }
        IntPair[] rtn = new IntPair[4];
        rtn[0] = min_adj_pt; // smallest "x"
        rtn[1] = min_opp_pt; // smallest "y"
        rtn[2] = max_adj_pt; // largest "x"
        rtn[3] = max_opp_pt; // largest "y"
        return rtn;
    }

    /*
     * Finds the key in a dictionary (map) with the largest value
     *
     * @param dict - the dictionary to search
     * @param IntPair - the key whose value is the maximum
     */
    IntPair find_key_with_largest_value(TreeMap<IntPair, Integer> dict) {
        Set<IntPair> keys = dict.keySet();
        int max_val = -100;
        IntPair max_key = null;
        for (IntPair key : keys) {
            int val = dict.get(key);
            if (val > max_val) {
                max_val = val;
                max_key = key;
            }
        }
        return max_key;
    }

    public TreeSet<IntPair> findCorners(Bitmap img) {
        return findCorners(img, img.getWidth()/2, img.getHeight()/2);
    }

    /*
     * Find the corners of a table near the center of the image
     *
     * @param img - the bitmap to find the table of
     * @return a set of corner-coordinates
     */
    public TreeSet<IntPair> findCorners(Bitmap img, int center_x, int center_y) {
        long start_time = System.currentTimeMillis();
        int w = img.getWidth();
        int h = img.getHeight();

        // compute a 2d array of intensities
        int[] intense = new int[w * h];
        /*
         * 0    -  255      intensities
         * 1000 - 1255      looked at
         * 2000 - 2255      table (painted)
         * 3000 - 3255      (bad) edge
         * 4000 - 4255      looked at
         * 5000 - 5255      (good) edge
         */
        int[][] magic = new int[w][h];
        img.getPixels(intense, 0, w, 0, 0, w, h);
        int red, green, blue, i;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                i = x + y * w;
                red   = (0xFF000000 & intense[i]) / 16777216;
                green = (0x00FF0000 & intense[i]) / 65536;
                blue  = (0x0000FF00 & intense[i]) / 256;
                magic[x][y] = (red + green + blue) / 3;
            }
        }
        long processing_time = System.currentTimeMillis();


        // "paint" the table
        CrystalCustomQueue pixels_to_look_at = new CrystalCustomQueue();
        pixels_to_look_at.enqueue(center_x, center_y);
        magic[w/2][h/2] += 1000;
        int threshold = 4;
        while (!pixels_to_look_at.is_empty()) {
            int pixel = pixels_to_look_at.dequeue();
            int pixel_x = pixel / 100000;
            int pixel_y = pixel % 100000;
            if (pixel_x != 0) {
                if (Math.abs(magic[pixel_x - 1][pixel_y]%1000 - magic[pixel_x][pixel_y]%1000) < threshold) {
                    if (magic[pixel_x-1][pixel_y] < 1000) {
                        magic[pixel_x-1][pixel_y] += 1000;
                        pixels_to_look_at.enqueue(pixel_x-1, pixel_y);
                    }
                }
            }
            if (pixel_y != 0) {
                if (Math.abs(magic[pixel_x][pixel_y - 1]%1000 - magic[pixel_x][pixel_y]%1000) < threshold) {
                    if (magic[pixel_x][pixel_y-1] < 1000) {
                        magic[pixel_x][pixel_y-1] += 1000;
                        pixels_to_look_at.enqueue(pixel_x, pixel_y-1);
                    }
                }
            }
            if (pixel_x != w - 1) {
                if (Math.abs(magic[pixel_x + 1][pixel_y]%1000 - magic[pixel_x][pixel_y]%1000) < threshold) {
                    if (magic[pixel_x+1][pixel_y] < 1000) {
                        magic[pixel_x+1][pixel_y] += 1000;
                        pixels_to_look_at.enqueue(pixel_x+1, pixel_y);
                    }
                }
            }
            if (pixel_y != h - 1) {
                if (Math.abs(magic[pixel_x][pixel_y + 1]%1000 - magic[pixel_x][pixel_y]%1000) < threshold) {
                    if (magic[pixel_x][pixel_y+1] < 1000) {
                        magic[pixel_x][pixel_y+1] += 1000;
                        pixels_to_look_at.enqueue(pixel_x, pixel_y+1);
                    }
                }
            }
            magic[pixel_x][pixel_y] += 1000;
        }
        long painting_time = System.currentTimeMillis();


        // find the edges (and lots of noise in the table)
        IntPair farthest_point = new IntPair(w/2, h/2);
        double max_dist = 0;
        for (int x = 1; x < w - 1; ++x) {
            for (int y = 1; y < h - 1; ++y) {
                if (magic[x][y] < 1000) {
                    if ((2000 <= magic[x+1][y] && magic[x+1][y] < 3000) || (2000 <= magic[x-1][y] && magic[x-1][y] < 3000) || (2000 <= magic[x][y-1] && magic[x][y-1] < 3000) || (2000 <= magic[x][y+1] && magic[x][y+1] < 3000)) {
                        magic[x][y] = (magic[x][y]%1000) + 3000;
                        double dist = (x-w/2.0)*(x-w/2.0) + (y-h/2.0)*(y-h/2.0);
                        if (dist > max_dist) {
                            max_dist = dist;
                            farthest_point.x = x;
                            farthest_point.y = y;
                        }
                    }
                }
            }
        }
        long borders_time = System.currentTimeMillis();

        // eliminate the noise in the middle of the table
        TreeSet<IntPair> pixels_to_look_at_b = new TreeSet<IntPair>();
        pixels_to_look_at_b.add(farthest_point);
        while (!pixels_to_look_at_b.isEmpty()) {
            IntPair pixel = pixels_to_look_at_b.first();
            magic[pixel.x][pixel.y] = (magic[pixel.x][pixel.y] % 1000) + 5000;
            pixels_to_look_at_b.remove(pixel);
            if (pixel.x != 0 && 3000 < magic[pixel.x - 1][pixel.y] && magic[pixel.x - 1][pixel.y] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.x != magic.length-1 && 3000 < magic[pixel.x + 1][pixel.y] && magic[pixel.x + 1][pixel.y] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.y != 0 && 3000 < magic[pixel.x][pixel.y - 1] && magic[pixel.x][pixel.y - 1] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x, pixel.y - 1);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.y != magic[0].length-1 && 3000 < magic[pixel.x][pixel.y + 1] && magic[pixel.x][pixel.y + 1] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x, pixel.y + 1);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.x != 0 && pixel.y != 0 && 3000 < magic[pixel.x - 1][pixel.y - 1] && magic[pixel.x - 1][pixel.y - 1] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y - 1);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.x != magic.length-1 && pixel.y != 0 && 3000 < magic[pixel.x + 1][pixel.y - 1] && magic[pixel.x + 1][pixel.y - 1] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y - 1);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.x != 0 && pixel.y != magic[0].length-1 && 3000 < magic[pixel.x - 1][pixel.y + 1] && magic[pixel.x - 1][pixel.y + 1] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y + 1);
                pixels_to_look_at_b.add(new_pixel);
            }
            if (pixel.x != magic.length-1 && pixel.y != magic[0].length-1 && 3000 < magic[pixel.x + 1][pixel.y + 1] && magic[pixel.x + 1][pixel.y + 1] < 5000) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y + 1);
                pixels_to_look_at_b.add(new_pixel);
            }
        }
        long cleaning_time = System.currentTimeMillis();


        // find the most extreme pixels over various rotations of the coordinate actions
        TreeMap<IntPair, Integer> extrema = new TreeMap<IntPair, Integer>();
        for (double theta = 0; theta < Math.PI/2; theta += 10 * Math.PI/180) {
            IntPair[] set = find_extrema(magic, theta);
            for (i = 0; i < set.length; ++i) {
                if (extrema.containsKey(set[i]))
                    extrema.put(set[i], extrema.get(set[i])+1);
                else
                    extrema.put(set[i], 1);
            }
        }
        long extrema_time = System.currentTimeMillis();

        // condense these extrema into 4 (likely) corners
        for (IntPair key : extrema.keySet()) {
            for (IntPair key2 : extrema.keySet()) {
                int dist = (key.x-key2.x)*(key.x-key2.x) + (key.y-key2.y)*(key.y-key2.y);
                if (key == key2)
                    continue;
                if (dist < 20) {
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
        TreeSet<IntPair> corners = new TreeSet<IntPair>();
        for (i = 0; i < 4; ++i) {
            IntPair corner = find_key_with_largest_value(extrema);
            corners.add(corner);
            extrema.remove(corner);
        }
        long corner_time = System.currentTimeMillis();

        System.out.println("---Times---");
        System.out.println(processing_time - start_time);
        System.out.println(painting_time - processing_time); // time hog
        System.out.println(borders_time - painting_time);
        System.out.println(cleaning_time - borders_time);
        System.out.println(extrema_time - cleaning_time);
        System.out.println(corner_time - extrema_time);

        return corners;
    }
}
