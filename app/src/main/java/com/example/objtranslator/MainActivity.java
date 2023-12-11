package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the user is already authenticated
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            setContentView(R.layout.activity_homepage);
        } else {
            // User is not authenticated, redirect to the registration page
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void gotoHomePage(View view) {
        setContentView(R.layout.activity_homepage);
    }

    public void gotoLogin(View view) {
        setContentView(R.layout.activity_login);
    }

    public void gotoRegister(View view) {
        setContentView(R.layout.activity_register);
    }

    public void continueAsGuest(View view) {
        // Assuming this is a guest mode
        Intent intent = new Intent(this, ObjTranslatorActivity.class);
        startActivity(intent);
    }

    public void gotoTranslate(View view) {
        Intent intent = new Intent(this, ObjTranslatorActivity.class);
        startActivity(intent);
        finish();
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}