package com.example.objtranslator.main;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.objtranslator.helpers.DotsOutline;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.example.objtranslator.helpers.ImageHelperActivity;

import java.util.ArrayList;
import java.util.List;

public class ObjTranslatorActivity extends ImageHelperActivity {

    private ObjectDetector objectDetector;
    TextView translated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);
    }

    protected void runTranslation(String object){
        //Create Translator from English to Chinese
        TranslatorOptions options = new TranslatorOptions.Builder()
                            .setTargetLanguage("zh")
                            .setSourceLanguage("en")
                            .build();
        Translator translator = Translation.getClient(options);

        //Set up download process dialog
        ProgressDialog progressDialog = new ProgressDialog(ObjTranslatorActivity.this);
        progressDialog.setMessage("Downloading the translation model...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //Make sure the required translation model has been downloaded to the device
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                progressDialog.dismiss();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                progressDialog.dismiss();
                            }
                        });

        Task<String> result = translator.translate(object).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(ObjTranslatorActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ObjTranslatorActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void runClassification(Bitmap bitmap){
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        objectDetector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>(){
                    @Override
                    public void onSuccess(@NonNull List<DetectedObject> detectedObjects) {
                        if (!detectedObjects.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            List<DotsOutline> dots = new ArrayList<>();
                            for (DetectedObject object : detectedObjects) {
                                if (!object.getLabels().isEmpty()) {
                                    //store first label
                                    String label = object.getLabels().get(0).getText();
                                    builder.append(label).append("\n");
                                    dots.add(new DotsOutline(object.getBoundingBox(), label));
                                    Log.d("ObjectDetection", "Object detected: " + label);
                                    runTranslation(label);
                                }
                            }
                            if(builder.length() == 0) builder.append("Unknown").append("\n");
                            getOutputTextView().setText(builder.toString());
                            drawDetectionResult(dots, bitmap);
                        } else {
                            getOutputTextView().setText("Could not detect");
                        }
                    }
                })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
            }
        });
    }
}