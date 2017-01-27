package com.crystal_ar.crystal_ar;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI;

public class CrystalAR {

        private TessBaseAPI mTess;    //Tess API reference
        private String datapath = ""; //path to folder containing language data file
        private String OCRresult;     // result from processImage
        private Context appContext;      //context of the user's application
        Word[] words;

        /*
         * @param context - context of the user's application: getApplicationContext()
         */
        public CrystalAR(Context context) {
            appContext = context;
            datapath = appContext.getFilesDir()+ "/tesseract/";
            mTess = new TessBaseAPI();
        }

        /*
        * Sets the language of Tesseract.
        * @param language - language(s) for Tesseract to track. For multiple languages add a '+'
        *                   between each language. Example: "eng+deu" for English and German.
        */
        public void setLanguage(String language) {
            for (String lang: language.split("\\+")) {
                checkFile(new File(datapath + "tessdata/"), lang);
            }
            mTess.init(datapath, language);
        }

        private void copyFile(String lang) {
            try {
                //location we want the file to be at
                String tessdataLangFile = "tessdata/" + lang + ".traineddata";
                String filepath = datapath + "/" + tessdataLangFile;

                //get access to AssetManager
                AssetManager assetManager = appContext.getAssets();

                //open byte streams for reading/writing
                InputStream instream = assetManager.open(tessdataLangFile);
                OutputStream outstream = new FileOutputStream(filepath);

                //copy the file to the location specified by filepath
                byte[] buffer = new byte[1024];
                int read;
                while ((read = instream.read(buffer)) != -1) {
                    outstream.write(buffer, 0, read);
                }
                outstream.flush();
                outstream.close();
                instream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void checkFile(File dir, String lang) {
            //directory does not exist, but we can successfully create it
            if (!dir.exists() && dir.mkdirs()) {
                copyFile(lang);
            }
            //The directory exists, but there is no data file in it
            if (dir.exists()) {
                String datafilepath = datapath + "/tessdata/"+ lang + ".traineddata";
                File datafile = new File(datafilepath);
                if (!datafile.exists()) {
                    copyFile(lang);
                }
            }
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

        /*
         * Gives an arraylist of phonenumbers from the previously processed image.
         */
        public ArrayList<String> getPhoneNumbers() {
            // Regex patterns that match:
            // 1. [+(]dd[)][- .]dd[- .]dd[- .]dd[- .]dd
            // 2. [+(]dd[)][- .]dd[- .]dd[- .]dd
            // 3. [+(]dd[)][- .]dd[- .]dd
            String reg = "([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})|" +
                         "([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})|" +
                         "([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})";

            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(OCRresult);

            ArrayList<String> phoneNumbers = new ArrayList<String>();

            while (matcher.find()) {
                phoneNumbers.add(matcher.group());
            }

            return phoneNumbers;
        }
    }

