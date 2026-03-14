package com.sahayak.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Execution;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmergencyVideoService extends Service implements LifecycleOwner {
    private static final String TAG = "EmergencyVideoService";
    private static final String CHANNEL_ID = "EmergencyVideoChannel";
    
    private LifecycleRegistry lifecycleRegistry;
    private ExecutorService cameraExecutor;
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;

    @Override
    public void onCreate() {
        super.onCreate();
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        cameraExecutor = Executors.newSingleThreadExecutor();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(3, createNotification());
        lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        startCameraAndRecording();
        return START_STICKY;
    }

    private void startCameraAndRecording() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.SD)) // SD for faster saving/background
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, videoCapture);

                recordVideo();

            } catch (Exception e) {
                Log.e(TAG, "Camera initialisation failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void recordVideo() {
        String name = "REC_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault()).format(new Date());
        
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Sahayak/Emergency_Recordings");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        currentRecording = videoCapture.getOutput()
                .prepareRecording(this, mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), recordEvent -> {
                    if (recordEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                        if (!finalizeEvent.hasError()) {
                            Log.d(TAG, "Video saved to gallery successfully");
                        } else {
                            Log.e(TAG, "Video recording error: " + finalizeEvent.getError());
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (currentRecording != null) {
            currentRecording.stop();
        }
        lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        cameraExecutor.shutdown();
        super.onDestroy();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Emergency Video Recording", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sahayak Video System")
                .setContentText("Recording emergency video evidence...")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }
}
