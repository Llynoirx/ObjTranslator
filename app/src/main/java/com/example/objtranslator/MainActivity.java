package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

import android.util.Log;
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
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
            }
        }

        public void gotoHomePage() {
            setContentView(R.layout.activity_homepage);
        }


        public void gotoTranslate(View view) {
            Intent intent = new Intent(this, CameraUploadActivity.class);
            startActivity(intent);
        }

        public void logout(View view) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
}
