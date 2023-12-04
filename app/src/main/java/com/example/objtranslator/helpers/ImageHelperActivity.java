package com.example.objtranslator.helpers;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.objtranslator.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageHelperActivity extends AppCompatActivity {

    private int REQUEST_PICK_IMG = 1000;
    private int REQUEST_CAPTURE_IMG = 1001;
    private ImageView inputImageView;
    private TextView outputTextView;
    private File photoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        inputImageView = findViewById(R.id.imageViewInput);
        outputTextView = findViewById(R.id.textViewOutput);

        //Ask user for permission to read external storage first time around
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    //Debug Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(ImageHelperActivity.class.getSimpleName(), "grant result for " + permissions[0] + " is " + grantResults[0]);
    }

    public void onPickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent, REQUEST_PICK_IMG);
    }

    public void onStartCamera(View view) {
        photoFile = createPhotoFile();

        Uri fileUri = FileProvider.getUriForFile(this, "com.example.fileprovider", photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, REQUEST_CAPTURE_IMG);

    }

    private File createPhotoFile() {
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ML_IMAGE_HELPER");

        //Create directory to store photos taken by camera (if it doesnt already exist)
        if (!photoFileDir.exists()) {
            photoFileDir.mkdirs();
        }

        //Create image file and name it in date-time format
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(photoFileDir.getPath() + File.separator + name);
        return file;
    }


    //Load image from storage
    private Bitmap loadFromUri(Uri uri) throws IOException {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bitmap;
    }


    //Classify images; display all objects w/ confidence >= 70%.
    //If no objects could be clearly identified, output 'Could not classify'
    protected void runClassification(Bitmap bitmap) {}

    protected TextView getOutputTextView() {
        return outputTextView;
    }

    protected ImageView getInputImageView() {
        return inputImageView;
    }

    protected void drawDetectionResult(List<DotsOutline> dots, Bitmap bitmap){
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas  = new Canvas(outputBitmap);
        Paint penDot = new Paint();
        penDot.setColor(Color.GRAY);
        penDot.setStyle(Paint.Style.STROKE);
        penDot.setStrokeWidth(8f);

        Paint penLabel = new Paint();
        penLabel.setColor(Color.YELLOW);
        penLabel.setStyle(Paint.Style.FILL_AND_STROKE);
        penLabel.setTextSize(96f);
        penLabel.setStrokeWidth(2f);

        for (DotsOutline dotOutline : dots) {
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

        getInputImageView().setImageBitmap(outputBitmap);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Display image once user selects it + classifies it
        Uri uri;
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMG) {
                uri = data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = loadFromUri(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                inputImageView.setImageBitmap(bitmap);
                runClassification(bitmap);
            } else if (requestCode == REQUEST_CAPTURE_IMG) {
                Log.d("ML", "received callback from camera");
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(photoFile.getAbsolutePath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Log.d("EXIF", "Exif: " + orientation);
                    Matrix matrix = new Matrix();
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        matrix.postRotate(90);
                    }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        matrix.postRotate(180);
                    }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        matrix.postRotate(270);
                    }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                inputImageView.setImageBitmap(bitmap);
                runClassification(bitmap);
            }
        }
    }
}