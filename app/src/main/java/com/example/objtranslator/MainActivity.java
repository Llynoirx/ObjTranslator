package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.objtranslator.helpers.ImageHelperActivity;
import com.example.objtranslator.image.ImageClassificationActivity;
import com.example.objtranslator.image.ObjectDetectionActivity;

import android.content.Intent;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    //Homepage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //When clicking onGotoImage Button => Image Activity Page
    public void onGotoImageActivity(View view){
        //start image helper activity
        Intent intent = new Intent(this, ImageClassificationActivity.class);
        startActivity(intent);
    }

    //When clicking onGotoImage Button => Image Activity Page
    public void onGotoObjectDetection(View view){
        //start image helper activity
        Intent intent = new Intent(this, ObjectDetectionActivity.class);
        startActivity(intent);
    }
}