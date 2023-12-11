package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class ObjTranslatorActivity extends AppCompatActivity {

    private ImageView imgView;
    private TextView srcView;
    private TextView targView;
    private ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_img);

        imgView = findViewById(R.id.image);
        srcView = findViewById(R.id.srcLangObj);
        targView = findViewById(R.id.targLangObj);

        //Multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);

        //Multiple object detection in static images
//        LocalModel localModel = new LocalModel.Builder()
//                .setAbsoluteFilePath("/Users/kathyho/StudioProjects/ObjTranslator/app/src/main/ml/model.tflite")
//                .build();
//
//        CustomObjectDetectorOptions options =
//                new CustomObjectDetectorOptions.Builder(localModel)
//                        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
//                        .enableMultipleObjects()
//                        .enableClassification()
//                        .setClassificationConfidenceThreshold(0.5f)
//                        .build();
//
//        objectDetector = ObjectDetection.getClient(options);

        Bitmap bitmap = getIntent().getParcelableExtra("bitmapExtra");

        if (bitmap != null) {
            imgView.setImageBitmap(bitmap);
            runClassification(bitmap);
        }
    }


    private void runTranslation(String object){
        //Create Translator from English to Chinese
        TranslatorOptions options = new TranslatorOptions.Builder()
            .setSourceLanguage("en")
            .setTargetLanguage("zh")
            .build();
        Translator translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
            .requireWifi()
            .build();

        ProgressDialog progressDialog = new ProgressDialog(ObjTranslatorActivity.this);
        progressDialog.setMessage("Downloading the translation model...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        translator.downloadModelIfNeeded(conditions)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss(); //Dismiss dialog regardless of success or failure
                    if (task.isSuccessful()) {
                        // Model downloaded successfully. Okay to start translating.
                        translator.translate(object)
                                .addOnSuccessListener(s -> showToast(s))
                                .addOnFailureListener(e -> showToast(e.getMessage()));
                    } else {
                        // Model couldn’t be downloaded or other internal error.
                        showToast("Translation model download failed.");
                    }
                });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(ObjTranslatorActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    //Classify images; display all objects w/ confidence >= 70%.
    //If no objects could be clearly identified, output 'Could not classify'
    protected void runClassification(Bitmap bitmap){
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        objectDetector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>(){
                    @Override
                    public void onSuccess(@NonNull List<DetectedObject> detectedObjects) {
                        if (!detectedObjects.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            List<BoundingBoxActivity> dots = new ArrayList<>();
                            for (DetectedObject object : detectedObjects) {
                                if (!object.getLabels().isEmpty()) {
                                    //store first label
                                    String label = object.getLabels().get(0).getText();
                                    builder.append(label).append("\n");
                                    dots.add(new BoundingBoxActivity(object.getBoundingBox(), label));
                                    Log.d("ObjectDetection", "Object detected: " + label);
                                    runTranslation(label);
                                }
                            }
                            if(builder.length() == 0) builder.append("Unknown").append("\n");
                            srcView.setText(builder.toString());
                            drawDetectionResult(dots, bitmap);
                        } else {
                            srcView.setText("Could not detect");
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



    protected void drawDetectionResult(List<BoundingBoxActivity> dots, Bitmap bitmap){
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas  = new Canvas(outputBitmap);
        Paint penDot = new Paint();
        penDot.setColor(Color.BLUE);
        penDot.setStyle(Paint.Style.STROKE);
        penDot.setStrokeWidth(8f);

        Paint penLabel = new Paint();
        penLabel.setColor(Color.BLACK);
        penLabel.setStyle(Paint.Style.FILL_AND_STROKE);
        penLabel.setTextSize(96f);
        penLabel.setStrokeWidth(2f);

        for (BoundingBoxActivity dotOutline : dots) {
            canvas.drawRect(dotOutline.rect, penDot);

            // Rect
            Rect labelSize = new Rect(0,0,0,0);
            penLabel.getTextBounds(dotOutline.label, 0, dotOutline.label.length(), labelSize);

            float fontSize = penLabel.getTextSize() * dotOutline.rect.width() / labelSize.width();
            if (fontSize < penLabel.getTextSize()){
                penLabel.setTextSize(fontSize);
            }

            canvas.drawText(dotOutline.label, dotOutline.rect.left, dotOutline.rect.top + labelSize.height(), penLabel);
        }
        imgView.setImageBitmap(outputBitmap);
    }

}