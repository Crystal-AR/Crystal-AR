package com.crystal_ar.crystalcompsdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnTextActivity;
    private Button btnCornerActivity;
    private Button btnObjectActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTextActivity = (Button) findViewById(R.id.BtnTextActivity);
        btnCornerActivity = (Button) findViewById(R.id.BtnCornerActivity);
        btnObjectActivity = (Button) findViewById(R.id.BtnObjectActivity);

        btnTextActivity.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TextActivity.class);
                MainActivity.this.startActivity(intent);
            }


        });
    }
}
