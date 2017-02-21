package com.crystal_ar.crystal_ar;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI;

public class CrystalAR {
    public final static int CORNERS_FOUND = 2;
    public final static int IMAGE_PROCESSED = 1;

    private TessBaseAPI mTess;    //Tess API reference
    private String datapath = ""; //path to folder containing language data file
    private String OCRresult;     // result from processImage
    private Context appContext;      //context of the user's application
    private Bitmap img;
    private Word[] words;
    private ArrayList<Rect> rects;

    /*
     * @param context - context of the user's application: getApplicationContext()
     */
    public CrystalAR(Context context) {
        appContext = context;
        datapath = appContext.getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();
    }

    public CrystalAR(Context context, Bitmap image) {
        appContext = context;
        datapath = appContext.getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();
        // Default language of English.
        setLanguage("eng");
        img = image;
        processImage(img);
    }

    /*
    * Sets the language of Tesseract.
    * @param language - language(s) for Tesseract to track. For multiple languages add a '+'
    *                   between each language. Example: "eng+deu" for English and German.
    */
    public void setLanguage(String language) {
        for (String lang : language.split("\\+")) {
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
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
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
     * Returns a List<Word> with urls from the previously processed image.
     * @return List<Word> - urls.
     */
    public List<Word> getURLs() {
        List<Word> urlsFound = new ArrayList<Word>();
        for (Word word : words) {
            URL url;
            try {
                url = new URL(word.str);
                urlsFound.add(word);
            } catch (MalformedURLException e) {
                // skip this -- should probably handle this somehow...
            }
        }

        return urlsFound;
    }

    /*
     * Returns an List<Word> with phone numbers from the previously processed image.
     * @return List<Word> - phone numbers.
     */
    public List<Word> getPhoneNumbers() {
        String reg = "([\\+(]?(\\d){1,}[)]?([- \\.]?(\\d){2,}){1,4})";

        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(OCRresult);

        ArrayList<String> phoneNumberStrings = new ArrayList<String>();
        while (matcher.find()) {
            phoneNumberStrings.add(matcher.group());
        }

        int startIndex;
        int endIndex;
        int ocrResultIndex;
        Rect phoneNumberRect;
        List<Word> phoneNumberWords = new ArrayList<Word>();
        List<Word> phoneNumbers = new ArrayList<Word>();
        // Find the words corresponding to each phone number and create a new word for it.
        for (String phoneNumber : phoneNumberStrings) {
            // Get start and end index of the phone number in OCRresult.
            startIndex = OCRresult.indexOf(phoneNumber);
            // We subtract 1 because indices start at 0.
            endIndex = startIndex + phoneNumber.length() - 1;
            phoneNumbers.clear();

            // ocrResultIndex starts at -1 because index starts at 0 and we are adding the length
            // of the word to update the index.
            ocrResultIndex = -1;
            for (Word word : words) {
                // Increment ocrResultIndex by the length of the word.
                ocrResultIndex = ocrResultIndex + word.str.length();
                // If startIndex is less than or equal to the index, the word must be part of our
                // phoneNumber.
                if (startIndex <= ocrResultIndex) {
                    phoneNumbers.add(word);
                }

                // If the end index of the phone number is less than or equal to the index,
                // then surely the next word will not be part of the phone number, so we break.
                if (endIndex <= ocrResultIndex) {
                    break;
                }

                // We add 1 because words were found by splitting on spaces, but we need to add
                // this back into the index as the index is for OCRresult.
                ocrResultIndex++;
            }

            // Finds the rectangle for the phone number and adds a new Word for this phone number
            // to our list of Words.
            phoneNumberRect = createRect(phoneNumbers);
            phoneNumberWords.add(new Word(phoneNumber, phoneNumberRect));
        }

        return phoneNumberWords;
    }

    /*
     * Creates a Rect surrounding a given List<Word>.
     * @param List<Word> - Words for which to make a surrounding Rect.
     * @return Rect that surrounds the list of words.
     */
    public Rect createRect(List<Word> words) {
        int top = -1;
        int left = -1;
        int bottom = -1;
        int right = -1;

        // Finds the left-, top-, right-, bottom-most extreme values.
        for (Word word : words) {
            if (left == -1 || word.x < left) {
                left = word.x;
            }

            if (top == -1 || word.y < top) {
                top = word.y;
            }

            if (right == -1 || word.x + word.width > right) {
                right = word.x + word.width;
            }

            if (bottom == -1 || word.y + word.height > bottom) {
                bottom = word.y + word.height;
            }
        }

        return new Rect(left, top, right, bottom);
    }

    /*
     * Returns an List<Word> with emails from the previously processed image.
     * @return List<Word> - emails.
     */
    public List<Word> getEmails() {
        List<Word> emails = new ArrayList<Word>();
        for (Word word : words) {
            if (Patterns.EMAIL_ADDRESS.matcher(word.str).matches()) {
                emails.add(word);
            }
        }

        // Alternative:
        // Match against simple regex and then check for "proper" emails.
        // "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+"

        return emails;
    }

    /*
     * Allows the user to replace a word with a picture
     * @params: the original image, list of Strings to be replaced, and list of images replacing the words
     * @return: the modified image
     */
    public Bitmap replaceWithImage(Bitmap image, String[] toReplace, Bitmap[] toAddImages) {
        Bitmap newImg = image;
        int index = -1, i;
        Canvas canvas = new Canvas(newImg);
//            for(i=0; i< toReplace.length; i++) {
//                index = words.indexOf(toReplace[i]);
//                if (index != -1) {
//                    canvas.drawBitmap(toAddImages[i], null, rects[index], null);
//                    OCRresult = OCRresult.replaceFirst(word, "  "); //replace with two blank spaces.
//                }
//            }
        return newImg;
    }

    public ProcessImageRunnable getProcessImageRunnable(Handler handler, Bitmap image) {
        return new ProcessImageRunnable(handler, image);
    }

    private class ProcessImageRunnable implements Runnable {
        Handler handler;
        Bitmap image;

        public ProcessImageRunnable(Handler handler, Bitmap image) {
            this.handler = handler;
            this.image = image;
        }

        public void run() {
            processImage(this.image);

            Message message = new Message();
            message.what = CrystalAR.IMAGE_PROCESSED;
            this.handler.sendMessage(message);
        }
    }

    public IntPair[] findCorners(Bitmap image){
        CornerFinder sm = new CornerFinder();
        IntPair[] corners = sm.findCorners(image);
        return corners;
    }

    public FindCornersRunnable findCornersRunnable(Handler handler, Bitmap image) {
        return new FindCornersRunnable(handler, image);
    }

    private class FindCornersRunnable implements Runnable {
        Handler handler;
        Bitmap image;

        public FindCornersRunnable(Handler handler, Bitmap image) {
            this.handler = handler;
            this.image = image;
        }

        public void run() {
            IntPair[] corners = findCorners(this.image);

            Message message = new Message();
            message.what = CrystalAR.CORNERS_FOUND;
            message.obj = corners;
            this.handler.sendMessage(message);
        }
    }
}

