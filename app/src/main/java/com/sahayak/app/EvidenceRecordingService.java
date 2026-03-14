package com.sahayak.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EvidenceRecordingService extends Service {
    private static final String TAG = "EvidenceRecording";
    private static final String CHANNEL_ID = "EvidenceRecordingChannel";
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(2, createNotification());
        startRecording();
        return START_STICKY;
    }

    private void startRecording() {
        if (isRecording) return;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "EVIDENCE_" + timeStamp + ".mp4";
        
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File outputFile = new File(storageDir, fileName);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // Note: For simplicity and background reliability, we start with Audio recording.
        // Video recording in background requires a hidden surface/camera overlay which is complex.
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Recording started: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Recording failed", e);
        }
    }

    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                Log.d(TAG, "Recording stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recorder", e);
            }
            mediaRecorder = null;
            isRecording = false;
        }
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Evidence Recording Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sahayak Evidence System")
                .setContentText("Recording audio evidence for your safety...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
    }
}
