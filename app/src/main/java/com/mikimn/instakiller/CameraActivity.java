package com.mikimn.instakiller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 10;
    private static final String[] PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // UI
    private SwitchMaterial postOnlineSwitch;
    private PreviewView previewView;

    // Image capture (CameraX)
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private CameraSelector cameraSelector;
    private ImageCapture imageCapture;
    private Preview preview;
    private Executor cameraExecutor;
    private Camera camera;

    // API
    private InstakillerAPI api = InstakillerAPI.instance();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        postOnlineSwitch = findViewById(R.id.post_online_switch);
        previewView = findViewById(R.id.camera_preview);
        previewView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        findViewById(R.id.close_button).setOnClickListener(v -> finish());
        if (!permissionsGranted(PERMISSIONS)) {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        } else {
            bindImageCapture();
        }
    }

    private boolean permissionsGranted(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String perm : permissions) {
            if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void bindImageCapture() {
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                cameraProvider.unbindAll();

                preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetRotation(Surface.ROTATION_0)
                        .build();

                imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetRotation(Surface.ROTATION_0)
                        .build();

                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(Surface.ROTATION_0)
                        .build();

                cameraExecutor = Executors.newSingleThreadExecutor();

                camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, imageAnalysis, preview);
                preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));
                findViewById(R.id.take_picture_button).setOnClickListener(v -> dispatchTakePictureIntent());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void dispatchTakePictureIntent() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("dd-MM-YYYY_HH:mm", Locale.getDefault());
        File imageFile = new File(Environment.getExternalStorageDirectory(), "image-" + format.format(date) + ".jpeg");
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(imageFile).build();
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        api.uploadImage(imageFile, postOnlineSwitch.isActivated(), object -> {
                            Snackbar.make(previewView, "Successfully uploaded image!", Snackbar.LENGTH_LONG);
                            finish();
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        bindImageCapture();
    }
}
