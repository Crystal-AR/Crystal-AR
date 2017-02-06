package com.crystal_ar.crystal_ar;

import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;



public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_TAKE_PHOTO = 0;
    public static final int REQUEST_TAKE_VIDEO = 1;
    public static final int REQUEST_PICK_PHOTO = 2;
    public static final int REQUEST_PICK_VIDEO = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView mFactTextView;
    private Button mShowFactButton;

    private Uri mMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFactTextView = (TextView) findViewById(R.id.factTextView);
        mShowFactButton = (Button) findViewById(R.id.showFactButton);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // update textview with new fact
                String fact = "Ostriches can run faster than horses.";
                mFactTextView.setText(fact);
            }
        };
        mShowFactButton.setOnClickListener(listener);
    }

    @OnClick(R.id.takePhoto)
    void takePhoto(){
        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        if (mMediaUri == null){
            Toast.makeText(this,
                    "There was a problem acccessing your device's external storage.",
                    Toast.LENGTH_LONG).show();
        }
        else {
            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
            startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @OnClick(R.id.takeVideo)
    void takeVideo(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
    }

    private Uri getOutputMediaFileUri(int mediaType) {
        // check for external storage
        if (isExternalStorageAvailable()) {
            // Get external storage directory
            File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // create file name
            String fileName = "";
            String fileType = "";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            if (mediaType == MEDIA_TYPE_IMAGE) {
                fileName = "IMG_"+ timeStamp;
                fileType = ".jpg";
            } else if(mediaType == MEDIA_TYPE_VIDEO) {
                fileName = "VID_"+ timeStamp;
                fileType = ".mp4";
            } else {
                return null;
            }

            // Create file
            try {
                File mediaFile = File.createTempFile(fileName, fileType, mediaStorageDir);
                Log.i(TAG, "File: " + Uri.fromFile(mediaFile));

                // return the file URI
                return Uri.fromFile(mediaFile);
            }

            catch (IOException e) {
                Log.e(TAG, "Error creaking file: " + mediaStorageDir.getAbsolutePath() +fileName + fileType);
            }
        }

        // something went wrong
        return null;

    }

    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        else {
            return false;
        }
    }
}
