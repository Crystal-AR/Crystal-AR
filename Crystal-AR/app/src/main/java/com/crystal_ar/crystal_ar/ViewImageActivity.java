/**
 * Created by simonorlovsky on 2/5/17.
 */

package com.crystal_ar.crystal_ar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    public static final String TAG = ViewImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        Intent intent = getIntent();
        Uri imageUri = intent.getData();
        Picasso.with(this).load(imageUri).into(imageView);
    }
}