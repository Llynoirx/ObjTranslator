package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    private boolean userIsRegistered() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (userIsRegistered()) {
            setContentView(R.layout.activity_homepage);
        } else {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
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
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}