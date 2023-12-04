package com.example.objtranslator.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.objtranslator.R;
import com.example.objtranslator.other.ImageClassificationActivity;

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
        Intent intent = new Intent(this, ObjectTranslatorActivity.class);
        startActivity(intent);
    }
}