package com.example.objtranslator;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
//import com.google.mlkit.vision.objects.ObjectDetector;
//import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.support.image.TensorImage;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjTranslatorActivity extends AppCompatActivity {

    private ImageView imgView;
    private TextView srcView, targView;
    private ObjectDetector detector;

    private List<ObjInfo> objects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_translate_img);

        imgView = findViewById(R.id.image);
        srcView = findViewById(R.id.srcLangObj);
        targView = findViewById(R.id.targLangObj);

        String filePath = getIntent().getStringExtra("filePath");

        if (filePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

            if(objects != null){
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
                                String clickedLabel = clickedObject.getLabel();
                                srcView.setText(clickedLabel);
                                runTranslation(srcView.getText().toString());
                            }
                        }

                        return true;
                    }
                });
            }
            try {
                runClassification(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            imgView.setImageBitmap(bitmap);

        } else {
            showToast("File path is null");
        }
    }

    //Classify images; display all objects w/ confidence >= 70%.
    //If no objects could be clearly identified, output 'Could not classify'
    // Classify images; display all objects with confidence >= 70%.
    // If no objects could be clearly identified, output 'Could not classify'
//    protected void runClassification(Bitmap bitmap) throws IOException {
//        // Step 1: Create TFLite's TensorImage object
//        TensorImage inputImage = TensorImage.fromBitmap(bitmap);
//
//        // Step 2: Initialize the detector object
//        ObjectDetector.ObjectDetectorOptions.Builder optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder();
//        optionsBuilder.setMaxResults(5);
//        optionsBuilder.setScoreThreshold(0.3f);
//        ObjectDetector.ObjectDetectorOptions options = optionsBuilder.build();
//
//        ObjectDetector detector = ObjectDetector.createFromFileAndOptions(
//                this,
//                "model.tflite",
//                options
//        );
//        // Step 3: Feed given image to the detector
//        List<Detection> detectedObjects = detector.detect(inputImage);
//
//        if(detectedObjects!=null) {
//            // Step 4: Parse the detection result and show it
//            for (Detection object : detectedObjects) {
//                // Get the top-1 category and craft the display text
//                Category category = object.getCategories().get(0);
//                String label = category.getLabel();
//
//                // Create a data object to display the detection result
//                if (label != null && !label.isEmpty()) {
//                    RectF rectF = object.getBoundingBox();
//                    Rect rect = new Rect();
//                    rectF.roundOut(rect);
//                    objects.add(new ObjInfo(rect, label));
//                }
//            }
//            drawDetectionResult(objects, bitmap);
//        }
//    }

    protected void runClassification(Bitmap bitmap) throws IOException {
        // Step 1: Create TFLite's TensorImage object
        TensorImage inputImage = TensorImage.fromBitmap(bitmap);

        List<String> labels = readLabelsFromFile("label.txt");

        // Step 2: Initialize the detector object
        ObjectDetector.ObjectDetectorOptions.Builder optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder();
        optionsBuilder.setMaxResults(5);
        optionsBuilder.setScoreThreshold(0.5f);
        optionsBuilder.setLabelAllowList(labels);
        ObjectDetector.ObjectDetectorOptions options = optionsBuilder.build();

        ObjectDetector detector = ObjectDetector.createFromFileAndOptions(
                this,
                "model.tflite",
                options
        );

        // Step 3: Feed given image to the detector
        List<Detection> detectedObjects = detector.detect(inputImage);

        // Clear previous detection results
        objects.clear();

        if (detectedObjects != null) {
            // Step 4: Parse the detection result and show it
            for (Detection object : detectedObjects) {
                // Get the top-1 category and craft the display text
                Category category = object.getCategories().get(0);
                String label = category.getLabel();

                Log.d("ObjectDetection", "Label: " + label + ", BoundingBox: " + object.getBoundingBox());

                // Create a data object to display the detection result
                if (label != null && !label.isEmpty()) {
                    objects.add(new ObjInfo(object.getBoundingBox(), label));
                    Log.d("ObjectDetection", "Object detected: " + label);
                }
            }

            // Step 5: Draw the detection result
            if(!objects.isEmpty()) {
                Log.d("OBJECT LIST", "Detected objects: " + objects.toString());
                drawDetectionResult(objects, bitmap);
            } else {
                Log.d("OBJECT EMPTY", "objects empty");
            }
        } else {
            targView.setText("Unable to detect");
            srcView.setText(":(");
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
                    progressDialog.dismiss(); // Dismiss dialog regardless of success or failure
                    if (task.isSuccessful()) {
                        // Model downloaded successfully. Okay to start translating.
                        translator.translate(object)
                                .addOnSuccessListener(s -> {
                                    // Update targView with the translated text
                                    targView.setText(s);
                                })
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

    private void drawDetectionResult(List<ObjInfo> detectedObjects, Bitmap bitmap) {
        Log.d("IN DRAW DETCTION", "in function");
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint penObj = new Paint();
        penObj.setColor(Color.RED);
        penObj.setStrokeWidth(8F);
        penObj.setStyle(Paint.Style.FILL);

        for (ObjInfo object : detectedObjects) {
            RectF box = object.getBoundingBox();
            float centerX = box.centerX();
            float centerY = box.centerY();
            canvas.drawCircle(centerX, centerY, 20f, penObj);
        }
        imgView.setImageBitmap(outputBitmap);
        Log.d("ImageView", "Bitmap set in ImageView");
        Log.d("DETECTION FINISHED", "in function");

    }

//    private void drawDetectionResult(List<ObjInfo> detectionResults, Bitmap bitmap) {
//        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(outputBitmap);
//        Paint pen = new Paint();
//        pen.setTextAlign(Paint.Align.LEFT);
//
//        for (ObjInfo result : detectionResults) {
//            // draw bounding box
//            pen.setColor(Color.RED);
//            pen.setStrokeWidth(8F);
//            pen.setStyle(Paint.Style.STROKE);
//            RectF box = result.getBoundingBox();
//            canvas.drawRect(box, pen);
//
//            Rect tagSize = new Rect(0, 0, 0, 0);
//
//            // calculate the right font size
//            pen.setStyle(Paint.Style.FILL_AND_STROKE);
//            pen.setColor(Color.YELLOW);
//            pen.setStrokeWidth(2F);
//
//            pen.setTextSize(96F);
//            pen.getTextBounds(result.getLabel(), 0, result.getLabel().length(), tagSize);
//            float fontSize = pen.getTextSize() * box.width() / tagSize.width();
//
//            // adjust the font size so texts are inside the bounding box
//            if (fontSize < pen.getTextSize()) pen.setTextSize(fontSize);
//
//            float margin = (box.width() - tagSize.width()) / 2.0F;
//            if (margin < 0F) margin = 0F;
//            canvas.drawText(
//                    result.getLabel(), box.left + margin,
//                    box.top + tagSize.height() * 1F, pen
//            );
//        }
//        imgView.setImageBitmap(outputBitmap);
//    }


    private int findClickedObjectIndex(float clickX, float clickY, List<ObjInfo> detectedObjects) {
        for (int i = 0; i < detectedObjects.size(); i++) {
            ObjInfo object = detectedObjects.get(i);
            RectF boundingBox = object.getBoundingBox();
            // Check if the click coordinates are within the bounding box
            if (clickX >= boundingBox.left && clickX <= boundingBox.right
                    && clickY >= boundingBox.top && clickY <= boundingBox.bottom) {
                return i;
            }
        }
        return -1;
    }

    public static List<String> readLabelsFromFile(String filePath) {
        List<String> labels = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming each line in the file represents a label
                labels.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception based on your requirements
        }

        return labels;
    }
}

