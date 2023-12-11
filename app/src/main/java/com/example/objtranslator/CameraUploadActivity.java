package com.example.objtranslator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraUploadActivity extends AppCompatActivity {

    private final int REQUEST_PICK_IMG = 1000;
    private final int REQUEST_CAPTURE_IMG = 1001;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_upload);

        //Ask user for permission to read external storage & access camera first time around
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }


    public void onUploadImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMG);
    }

    public void onStartCamera(View view) {
        photoFile = createPhotoFile();
        if(photoFile != null){
            Uri fileUri = FileProvider.getUriForFile(this, "com.example.fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, REQUEST_CAPTURE_IMG);
        }
    }

    private File createPhotoFile() {
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ML_IMAGE_HELPER");
        //Create directory to store photos taken by camera (if it doesn't already exist)
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

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File photoFile = createPhotoFile();
            FileOutputStream outputStream = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return photoFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                // Save bitmap to a file
                String filePath = saveBitmapToFile(bitmap);
                // Pass the file path to the next activity
                Intent intent = new Intent(this, ObjTranslatorActivity.class);
                intent.putExtra("filePath", filePath);
                startActivity(intent);
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
                    // Save bitmap to a file
                    String filePath = saveBitmapToFile(bitmap);
                    // Pass the file path to the next activity
                    Intent intent = new Intent(this, ObjTranslatorActivity.class);
                    intent.putExtra("filePath", filePath);
                    startActivity(intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}