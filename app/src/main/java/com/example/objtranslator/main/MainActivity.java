package com.example.objtranslator.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.objtranslator.R;
import com.example.objtranslator.onboarding.LoginActivity;
import com.example.objtranslator.other.ImageClassificationActivity;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    //Homepage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGotoImageActivity(View view){
        Intent intent = new Intent(this, ImageClassificationActivity.class);
        startActivity(intent);
    }

    public void onGotoObjectDetection(View view){
        Intent intent = new Intent(this, ObjectTranslatorActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}