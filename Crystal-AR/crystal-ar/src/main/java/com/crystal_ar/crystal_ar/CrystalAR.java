package com.crystal_ar.crystal_ar;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.TessBaseAPI;

/*
 * Public class: CrystalAR.
 * This class is the point of access to every feature that Crystal-AR offers.
 * Properties: n/a.
 * Methods: setLanguage, getPrimitiveString, processImage, getWords, getURLs, getPhoneNumbers,
 *          getEmails, createRect, replaceWithImage, getProcessImageRunnable, findCorners,
 *          findCornersRunnable.
 * @params Context - context of the user's application.
 * @params Bitmap (optional) - image to process with Tesseract.
 * @return n/a.
 */
public class CrystalAR {
    // Integers used to differentiate between the results returned by runnables.
    public final static int CORNERS_FOUND = 2;
    public final static int IMAGE_PROCESSED = 1;

    private TessBaseAPI mTess; // Tess API reference.
    private String datapath = ""; // Path to folder containing language data file.
    private String OCRresult; // Result from processImage.
    private Context appContext; // Context of the user's application.
    private Bitmap img; // Image.
    private Word[] words; // List of Words found in processed image.
    private ArrayList<Rect> rectList; // List of rectangles surrounding Words.
    CornerFinder cornerFinder = new CornerFinder(); // Object used to find corners.

    /*
     * Constructor.
     * @params Context - context of the user's application: getApplicationContext().
     * @return n/a.
     */
    public CrystalAR(Context context) {
        appContext = context;
        datapath = appContext.getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();
    }

    /*
     * Constructor.
     * @params Context - context of the user's application: getApplicationContext().
     * @params Bitmap - image to be processed.
     * @return n/a.
     */
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
     * @params String - language(s) for Tesseract to track. For multiple languages add a '+'
     *                   between each language. Example: "eng+deu" for English and German.
     * @return n/a.
     */
    public void setLanguage(String language) {
        for (String lang : language.split("\\+")) {
            checkFile(new File(datapath + "tessdata/"), lang);
        }
        mTess.init(datapath, language);
    }

    /*
     * Copies Tesseract language files.
     * @params String - language to be copied.
     * @return n/a.
     */
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

    /*
     * Checks whether directory and Tesseract language file exists.
     * @params File - directory containing Tesseract language files.
     * @params String - language.
     * @return n/a.
     */
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

    /*
     * Gets the raw Tesseract output.
     * @params n/a.
     * @return String - raw Tesseract output.
     */
    public String getPrimitiveString() {
        return OCRresult;
    }

    /*
     * Given an image, this runs Tesseract's main algorithm on it.
     * This method MUST be called before calling any other public methods.
     * @params Bitmap - image to analyze.
     * @return n/a.
     */
    public void processImage(Bitmap image) {
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();

        Pixa p = mTess.getWords();
        rectList = p.getBoxRects();
        String[] parts = OCRresult.split("\\s+");
        words = new Word[rectList.size()];
        for (int i = 0; i < rectList.size(); ++i) {
            if (i >= parts.length)
                break;
            words[i] = new Word(parts[i], rectList.get(i));
        }
    }

    /*
     * Returns a list of Word classes from the previously processed image
     * @params n/a.
     * @return Word[] - Words found by Tesseract.
     */
    public Word[] getWords() {
        Word[] arrayCopy = new Word[words.length];
        System.arraycopy(words, 0, arrayCopy, 0, words.length);
        return arrayCopy;
    }

    /*
     * Returns a List<Word> with urls from the previously processed image.
     * @params n/a.
     * @return List<Word> - urls.
     */
    public List<Word> getURLs() {
        List<Word> urlsFound = new ArrayList<Word>();
        String potentialURL;
        for (Word word : words) {
            URL url;
            try {
                potentialURL = word.str;
                if (word.str.length() > 6 && (word.str.substring(0, 4)).equals("www.")) {
                    potentialURL = "http://" + word.str;
                }

                url = new URL(potentialURL);
                urlsFound.add(word);
            } catch (MalformedURLException e) {
                // skip this -- should probably handle this somehow...
            }
        }

        return urlsFound;
    }

    /*
     * Returns an List<Word> with phone numbers from the previously processed image.
     * @params n/a.
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
     * @params List<Word> - Words for which to make a surrounding Rect.
     * @return Rect - Rectangle that surrounds the list of words.
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
     * @params n/a.
     * @return List<Word> - emails.
     */
    public List<Word> getEmails() {
        List<Word> emails = new ArrayList<Word>();
        for (Word word : words) {
            if (Patterns.EMAIL_ADDRESS.matcher(word.str).matches()) {
                emails.add(word);
            }
        }

        return emails;
    }

    /*
     * Allows the user to replace a word with a picture
     *
     * @params Bitmap - the image to modify.
     * @params String[] - strings to replace.
     * @params Bitmap[] - images to replace strings with.
     * @return: Bitmap - the modified image.
     */
    public Bitmap replaceWithImage(Bitmap image, String[] toReplace, Bitmap[] toAddImages) {
        Bitmap tempPhoto = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight());
        Canvas canvas = new Canvas(tempPhoto);
        // Draw the image bitmap into the canvas.
        Paint p = new Paint();
        p.setARGB(255, 255, 255, 255);
        canvas.drawBitmap(tempPhoto, 0, 0, null);
        int index;
        for(int i = 0; i < toReplace.length; i++) {
            index = findIndexOf(toReplace[i]);
            if (index != -1) {
                canvas.drawRect(rectList.get(index), p);
                canvas.drawBitmap(toAddImages[i], null, rectList.get(index), null);
                // replace with two blank spaces.
                OCRresult = OCRresult.replaceFirst(toReplace[i], "  ");
            }
        }
        return tempPhoto;
    }

    /*
     * Finds the index of a given string in 'words'.
     * @params String - string to find the index of.
     * @return int - index of string in 'words'.
     */
    private int findIndexOf(String el) {
        for(int i = 0; i < words.length; i++) {
            if (words[i].str.equalsIgnoreCase(el)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * Creates a runnable for processing an image.
     * @params Handler - handler that receives the result.
     * @params Bitmap - image to process.
     * @return ProcessImageRunnable - runnable.
     */
    public ProcessImageRunnable getProcessImageRunnable(Handler handler, Bitmap image) {
        return new ProcessImageRunnable(handler, image);
    }

    /*
     * Private class: ProcessImageRunnable.
     * This class is a runnable for processing images.
     * Methods: run.
     * @params Handler - handler that receives the result.
     * @params Bitmap - image to process.
     * @return n/a.
     */
    private class ProcessImageRunnable implements Runnable {
        Handler handler;
        Bitmap image;

        /*
         * Constructor.
         * @params Handler - handler that receives the result.
         * @params Bitmap - image to process.
         * @return n/a.
         */
        public ProcessImageRunnable(Handler handler, Bitmap image) {
            this.handler = handler;
            this.image = image;
        }

        /*
         * Executes processImage() on the given image and send the result to the handler.
         * @params n/a.
         * @return n/a.
         */
        public void run() {
            processImage(this.image);

            Message message = new Message();
            message.what = CrystalAR.IMAGE_PROCESSED;
            this.handler.sendMessage(message);
        }
    }

    /*
     * Finds corners in an image.
     * @params Bitmap - image to find corners in.
     * @return IntPair[] - list of corners.
     */
    public IntPair[] findCorners(Bitmap image){
        IntPair[] corners = null;
        try {
            corners = cornerFinder.findCorners(image);
        }
        catch (NullPointerException e) {
            return new IntPair[0];
        }
        catch (IllegalArgumentException e) {
            return new IntPair[0];
        }
        catch (RuntimeException e) {
            return new IntPair[0];
        }
        return corners;
    }

    /*
     * Finds corners in an image, assuming that the center of the table is at (cx, cy).
     * @params Bitmap - image to find corners in.
     * @params int - x-coordinate of the center of the table.
     * @params int - y-coordinate of teh center of the table.
     * @return IntPair[] - list of corners.
     */
    public IntPair[] findCorners(Bitmap image, int cx, int cy){
        IntPair[] corners = null;
        try {
            corners = cornerFinder.findCorners(image, cx, cy);
        }
        catch (NullPointerException e) {
            return new IntPair[0];
        }
        catch (IllegalArgumentException e) {
            return new IntPair[0];
        }
        catch (RuntimeException e) {
            return new IntPair[0];
        }
        return corners;
    }

    /*
     * Finds corners in an image, assuming that the center of the table is at (cx, cy) and that both
     * the paint and corner thresholds are met.
     * @params Bitmap - image to find corners in.
     * @params int - x-coordinate of the center of the table.
     * @params int - y-coordinate of teh center of the table.
     * @params int - paint threshold.
     * @params int - corner threshold.
     * @return IntPair[] - list of corners.
     */
    public IntPair[] findCorners(Bitmap image, int cx, int cy, int paint_threshold, int corner_threshold){
        IntPair[] corners = null;
        try {
            corners = cornerFinder.findCorners(image, cx, cy, paint_threshold, corner_threshold);
        }
        catch (NullPointerException e) {
            return new IntPair[0];
        }
        catch (IllegalArgumentException e) {
            return new IntPair[0];
        }
        catch (RuntimeException e) {
            return new IntPair[0];
        }
        return corners;
    }

    /*
     * Creates a runnable for finding corners in an image.
     * @params Handler - handler that receives the result.
     * @params Bitmap - image to find corners in.
     * @return FindCornersRunnable - runnable.
     */
    public FindCornersRunnable findCornersRunnable(Handler handler, Bitmap image) {
        return new FindCornersRunnable(handler, image);
    }

    /*
     * Private class: FindCornersRunnable.
     * This class is a runnable for finding corners.
     * Methods: run.
     * @params Handler - handler that receives the result.
     * @params Bitmap - image to find corners in.
     * @return n/a.
     */
    private class FindCornersRunnable implements Runnable {
        Handler handler;
        Bitmap image;

        /*
         * Constructor.
         * @params Handler - handler that receives the result.
         * @params Bitmap - image to process.
         * @return n/a.
         */
        public FindCornersRunnable(Handler handler, Bitmap image) {
            this.handler = handler;
            this.image = image;
        }

        /*
         * Executes findCorners() on the given image and sends the result to the handler.
         * @params n/a.
         * @return n/a.
         */
        public void run() {
            IntPair[] corners = findCorners(this.image);

            Message message = new Message();
            message.what = CrystalAR.CORNERS_FOUND;
            message.obj = corners;
            this.handler.sendMessage(message);
        }
    }
}