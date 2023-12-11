package com.example.objtranslator.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.objtranslator.R;
import com.example.objtranslator.onboarding.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;

import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    //Homepage
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_main));
        //setContentView(R.layout.activity_register);
    }

    public void onGotoObjDetection(View view){
        Intent intent = new Intent(this, ObjTranslatorActivity.class);
        startActivity(intent);
    }

//    public void onGotoCustomObjDetection(View view){
////        Intent intent = new Intent(this, CustomObjTranslator.class);
////        startActivity(intent);
//    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}