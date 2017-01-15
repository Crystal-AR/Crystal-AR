package com.crystal_ar.crystal_ar;


import android.graphics.Bitmap;
import android.graphics.Rect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI;

public class mainLibrary {

        private TessBaseAPI mTess;    //Tess API reference
        private String datapath = ""; //path to folder containing language data file
        private String OCRresult;     // result from processImage
        Word[] words;

        /*
         * @param path - just put 'getFilesDir() + "/tesseract/"'
         */
        public mainLibrary(String path) {
            String language = "eng";
            mTess = new TessBaseAPI();
            datapath = path;
            mTess.init(datapath, language);
        }

        public String getPrimitiveString() {
            return OCRresult;
        }

        /*
         * Given an image, this runs Tesseract's main algorithm on it. You MUST run this before calling
         * any other public methods.
         * @param image - image to analyze
         */
        public void processImage(Bitmap image) {
            mTess.setImage(image);
            long startTime = System.nanoTime();
            OCRresult = mTess.getUTF8Text();

            Pixa p = mTess.getWords();
            ArrayList<Rect> lst = p.getBoxRects();
            String[] parts = OCRresult.split("\\s+");
            words = new Word[lst.size()];
            for (int i = 0; i < lst.size(); ++i) {
                if (i >= parts.length)
                    break;
                words[i] = new Word(parts[i], lst.get(i));
            }
        }

        /*
         * Returns a list of Word classes from the previously processed image
         */
        public Word[] getWords() {
            Word[] arrayCopy = new Word[words.length];
            System.arraycopy(words, 0, arrayCopy, 0, words.length);
            return arrayCopy;
        }

        /*
         * Gives a list of urls from the previously processed image
         */
        public URL[] getURLs() {
            ArrayList<URL> urlsFound = new ArrayList<URL>();
            for (Word word : words) {
                URL url;
                try {
                    url = new URL(word.str);
                    urlsFound.add(url);
                } catch (MalformedURLException e) {
                    // skip this -- should probably handle this somehow...
                }
            }

            URL[] rtn = new URL[urlsFound.size()];
            for (int i = 0; i < urlsFound.size(); ++i)
                rtn[i] = urlsFound.get(i);

            return rtn;
        }
    }

