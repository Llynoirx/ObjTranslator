package com.example.objtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
                        // Model couldn’t be downloaded/other internal error.
                        showToast("Translation model download failed.");
                    }
                });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(ObjTranslatorActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    protected void drawDetectionResult(List<ObjInfo> detectedObjects, Bitmap bitmap){
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint penObj = new Paint();
        penObj.setColor(Color.RED);
        penObj.setStyle(Paint.Style.FILL);
        penObj.setStrokeWidth(8f);

        // Draw dot at the center of each detected object
        for (ObjInfo object : detectedObjects) {
            float centerX = object.rect.exactCenterX();
            float centerY = object.rect.exactCenterY();
            canvas.drawCircle(centerX, centerY, 20f, penObj);
        }
        imgView.setImageBitmap(outputBitmap);
    }

    //Classify images; display all objects w/ confidence >= 70%.
    //If no objects could be clearly identified, output 'Could not classify'
    // Classify images; display all objects with confidence >= 70%.
    // If no objects could be clearly identified, output 'Could not classify'
    protected void runClassification(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        objectDetector.process(inputImage)
                .addOnSuccessListener(detectedObjects -> {
                    List<ObjInfo> objects = new ArrayList<>();
                    for (DetectedObject object : detectedObjects) {
                        if (!object.getLabels().isEmpty()) {
                            String label = object.getLabels().get(0).getText();
                            if (label != null && !label.isEmpty()) {
                                objects.add(new ObjInfo(object.getBoundingBox(), label));
                                Log.d("ObjectDetection", "Object detected: " + label);
                            }
                        }
                    }
                    if(!objects.isEmpty()){
                        drawDetectionResult(objects, bitmap);
                        imgView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                float clickX = event.getX();
                                float clickY = event.getY();
                                int action = event.getAction();

                                // Handle touch events
                                if (action == MotionEvent.ACTION_UP) {
                                    int clickedIndex = findClickedObjectIndex(clickX, clickY, objects);
                                    if (clickedIndex != -1 && clickedIndex < objects.size()) {
                                        ObjInfo clickedObject = objects.get(clickedIndex);
                                        String clickedLabel = clickedObject.label;
                                        srcView.setText(clickedLabel);
                                    }
                                }
                                return true;
                            }
                        });
                    } else {
                        srcView.setText("Unknown Object");
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    srcView.setText("Could not detect any objects");
                });
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_translate_img);

        imgView = findViewById(R.id.image);
        srcView = findViewById(R.id.srcLangObj);
        targView = findViewById(R.id.targLangObj);

        String filePath = getIntent().getStringExtra("filePath");
            try {
                if (filePath != null) {
                    //Multiple object detection in static images
                    ObjectDetectorOptions options =
                            new ObjectDetectorOptions.Builder()
                                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                                    .enableMultipleObjects()
                                    .enableClassification()
                                    .build();
                    objectDetector = ObjectDetection.getClient(options);

                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    runClassification(bitmap);
                    imgView.setImageBitmap(bitmap);
                } else {
                        showToast("File path is null");
                    }
        } catch (Exception e) {
                e.printStackTrace();
                showToast("Error during object detection: " + e.getMessage());
            }
        // Now, set the OnClickListener after drawDetectionResult

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
    }

    private int findClickedObjectIndex(float clickX, float clickY, List<ObjInfo> detectedObjects) {
        for (int i = 0; i < detectedObjects.size(); i++) {
            ObjInfo object = detectedObjects.get(i);
            Rect boundingBox = object.rect;
            // Check if the click coordinates are within the bounding box
            if (clickX >= boundingBox.left && clickX <= boundingBox.right
                    && clickY >= boundingBox.top && clickY <= boundingBox.bottom) {
                return i;
            }
        }
        return -1;
    }

}