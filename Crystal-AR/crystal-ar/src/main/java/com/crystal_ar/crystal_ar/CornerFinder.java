package com.crystal_ar.crystal_ar;

import android.graphics.Bitmap;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Sabastian on 2/14/17.
 */

public class CornerFinder {
    /*
     * a method to help debuging by printing out samples of the table in a grid
     */
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
     * This function takes a list of border pixels and a theta.
     * It then rotates the coordinate axes by theta, and finds the points with the smallest and
     * largest points in terms of their (rotated) x- and y- coordiantes
     *
     * @param borders - 2d array in which 2 is a "point" in the set
     * @param theta - angle of the "x-axis"
     * @return array of the 4 extrema
     */
    private IntPair[] find_extrema(CrystalCustomQueue borders, double theta) {
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

    public IntPair[] findCorners(Bitmap img) {
        return findCorners(img, img.getWidth()/2, img.getHeight()/2);
    }

    private int compute_pixel_diff(int x, int y) {
        return Math.abs((0xFF000000 & x) - (0xFF000000 & y)) / 16777216 + Math.abs((0x00FF0000 & x) - (0x00FF0000 & y)) / 65536 + Math.abs((0x0000FF00 & x) - (0x0000FF00 & y)) / 256;
    }

    /*
     * Find the corners of a table near the center of the image
     *
     * @param img - the bitmap to find the table of
     * @return a set of corner-coordinates
     */
    public IntPair[] findCorners(Bitmap img, int center_x, int center_y) {
        int w = img.getWidth();
        int h = img.getHeight();

        // compute a 2d array of intensities
        int[] intense = new int[w * h];
        /*
         * 0      intensities
         * 1      looked at
         * 2      table (painted)
         * 3      (bad) edge
         * 4      looked at
         * 5      (good) edge
         */
        int[][] magic = new int[w][h];
        img.getPixels(intense, 0, w, 0, 0, w, h);

        // "paint" the table
        CrystalCustomQueue pixels_to_look_at = new CrystalCustomQueue();
        pixels_to_look_at.enqueue(center_x, center_y);
        ++magic[w/2][h/2];
        int threshold = 4;
        while (!pixels_to_look_at.is_empty()) {
            int pixel = pixels_to_look_at.dequeue();
            int pixel_x = pixel / 100000;
            int pixel_y = pixel % 100000;
            if (pixel_x != 0) {

                if (compute_pixel_diff(intense[(pixel_x-1) + pixel_y * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x-1][pixel_y] < 1) {
                        ++magic[pixel_x-1][pixel_y];
                        pixels_to_look_at.enqueue(pixel_x-1, pixel_y);
                    }
                }
            }
            if (pixel_y != 0) {
                if (compute_pixel_diff(intense[pixel_x + (pixel_y-1) * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x][pixel_y-1] < 1) {
                        ++magic[pixel_x][pixel_y-1];
                        pixels_to_look_at.enqueue(pixel_x, pixel_y-1);
                    }
                }
            }
            if (pixel_x != w - 1) {
                if (compute_pixel_diff(intense[(pixel_x+1) + pixel_y * w], intense[pixel_x + pixel_y * w]) < threshold) {
                    if (magic[pixel_x+1][pixel_y] < 1) {
                        ++magic[pixel_x+1][pixel_y];
                        pixels_to_look_at.enqueue(pixel_x+1, pixel_y);
                    }
                }
            }
            if (pixel_y != h - 1) {
                if (compute_pixel_diff(intense[pixel_x + (pixel_y+1) * w], intense[pixel_x + pixel_y * w]) < threshold) {
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
        CrystalCustomQueue pixels_to_look_at_b = new CrystalCustomQueue();
        CrystalCustomQueue border_pixels = new CrystalCustomQueue();
        pixels_to_look_at_b.enqueue(farthest_point.x, farthest_point.y);
        while (!pixels_to_look_at_b.is_empty()) {
            int pixel = pixels_to_look_at_b.dequeue();
            int x = pixel / 100000;
            int y = pixel % 100000;
            magic[x][y] = 5;
            border_pixels.enqueue(x, y);
            if (x != 0 && magic[x - 1][y] == 3) {
                pixels_to_look_at_b.enqueue(x-1, y);
                magic[x-1][y] = 4;
            }
            if (x != magic.length-1 && magic[x + 1][y] == 3) {
                pixels_to_look_at_b.enqueue(x+1, y);
                magic[x+1][y] = 4;
            }
            if (y != 0 && magic[x][y - 1] == 3) {
                pixels_to_look_at_b.enqueue(x, y-1);
                magic[x][y-1] = 4;
            }
            if (y != magic[0].length-1 && magic[x][y + 1] == 3) {
                pixels_to_look_at_b.enqueue(x, y+1);
                magic[x][y+1] = 4;
            }
            if (x != 0 && y != 0 && magic[x - 1][y - 1] == 3) {
                pixels_to_look_at_b.enqueue(x-1, y-1);
                magic[x-1][y-1] = 4;
            }
            if (x != magic.length-1 && y != 0 && magic[x + 1][y - 1] == 3) {
                pixels_to_look_at_b.enqueue(x+1, y-1);
                magic[x+1][y-1] = 4;
            }
            if (x != 0 && y != magic[0].length-1 && magic[x - 1][y + 1] == 3) {
                pixels_to_look_at_b.enqueue(x-1, y+1);
                magic[x-1][y+1] = 4;
            }
            if (x != magic.length-1 && y != magic[0].length-1 && magic[x + 1][y + 1] == 3) {
                pixels_to_look_at_b.enqueue(x+1, y+1);
                magic[x+1][y+1] = 4;
            }
        }



        // find the most extreme pixels over various rotations of the coordinate actions
        TreeMap<IntPair, Integer> extrema = new TreeMap<IntPair, Integer>();
        for (double theta = 0; theta < Math.PI/2; theta += 5 * Math.PI/180) {
            IntPair[] set = find_extrema(border_pixels, theta);
            for (int i = 0; i < set.length; ++i) {
                if (extrema.containsKey(set[i]))
                    extrema.put(set[i], extrema.get(set[i])+1);
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


        // create set to return, and return it
        IntPair[] corners = new IntPair[4];
        for (int i = 0; i < 4; ++i) {
            IntPair corner = find_key_with_largest_value(extrema);
            corners[i] = corner;
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
}

