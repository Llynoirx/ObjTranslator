package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    //Homepage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onGotoHomePage(View view){
        setContentView(R.layout.activity_homepage);
    }

    public void onGotoTranslate(View view){
        Intent intent = new Intent(this, ObjTranslatorActivity.class);
        startActivity(intent);
        setContentView(R.layout.activity_translate_actions);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}