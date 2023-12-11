//package com.example.objtranslator.main;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.objtranslator.R;
//import com.example.objtranslator.other.ImageClassificationActivity;
//
//public class GuestActivity extends AppCompatActivity {
//
//    @Override
//    //Homepage
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_guest);
//    }
//
//    public void onGotoImageActivity(View view){
//        Intent intent = new Intent(this, ImageClassificationActivity.class);
//        startActivity(intent);
//    }
//
//    public void onGotoObjectDetection(View view){
//        Intent intent = new Intent(this, ObjectTranslatorActivity.class);
//        startActivity(intent);
//    }
//}