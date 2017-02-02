package com.crystal_ar.crystal_ar;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_TAKE_PHOTO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("hello1", "world");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @OnClick(R.id.takePhoto)
    void takePhoto(){
        Log.d("hello2", "world");
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);

    };

    @OnClick(R.id.selfDestruct)
    void selfDestruct(){
        Log.d("hello3", "world");
        TextView tv1 = (TextView)findViewById(R.id.texto);
        tv1.setText("Hello");

    };
}
