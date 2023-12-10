package com.example.objtranslator.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.objtranslator.helpers.DotsOutline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.interfaces.Detector;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import org.tensorflow.lite.support.image.TensorImage;

import java.util.ArrayList;
import java.util.List;

public class CustomObjDetectorActivity extends ObjectTranslatorActivity {

    private ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        LocalModel localModel = new LocalModel.Builder()
//                .setAssetFilePath("custom.tflite")
//                .build();
//
//        CustomObjectDetectorOptions options =
//                new CustomObjectDetectorOptions.Builder(localModel)
//                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
//                        .enableMultipleObjects()
//                        .enableClassification()
//                        .build();
//
//        objectDetector = ObjectDetection.getClient(options);
    }



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