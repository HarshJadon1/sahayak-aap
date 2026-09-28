package com.sahayak.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;

public class EmergencyService extends Service {
    private static final String TAG = "EmergencyService";
    private static final String CHANNEL_ID = "EmergencyChannel";
    
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private FallDetector mFallDetector;
    private VoiceSOSManager mVoiceSOSManager;
    private LocationHelper mLocationHelper;
    private ContactManager mContactManager;
    private boolean isEmergencyActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        
        mLocationHelper = new LocationHelper(this);
        mContactManager = new ContactManager(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 1. Shake Detection
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> {
            if (count >= 3 && !isEmergencyActive) {
                Log.d(TAG, "Shake detected!");
                triggerEmergencyMode("Shake Gesture");
            }
        });

        // 2. Fall Detection
        mFallDetector = new FallDetector();
        mFallDetector.setOnFallListener(() -> {
            if (!isEmergencyActive) {
                Log.d(TAG, "Fall detected!");
                showAccidentAlert();
            }
        });

        // 3. Voice SOS Detection
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            mVoiceSOSManager = new VoiceSOSManager(this, () -> {
                if (!isEmergencyActive) {
                    Log.d(TAG, "Voice Command detected!");
                    triggerEmergencyMode("Voice Command");
                }
            });
            mVoiceSOSManager.startListening();
        }
        
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mFallDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("TRIGGER_SOS".equals(action)) {
                triggerEmergencyMode("Manual/Accident");
            } else if ("STOP_SOS".equals(action)) {
                stopEmergencyMode();
            }
        }

        updateNotification("Sahayak Safety Active", "Shake or Say 'Emergency' to trigger SOS");
        return START_STICKY;
    }

    private void showAccidentAlert() {
        Intent alertIntent = new Intent(this, AccidentAlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alertIntent);
    }

    private void triggerEmergencyMode(String source) {
        if (isEmergencyActive) return;
        
        List<String> contacts = mContactManager.getContacts();
        if (contacts.isEmpty()) {
            // Fallback: No contacts found, call 112
            startFallbackCall();
            return;
        }

        isEmergencyActive = true;
        startEmergencyVideoService();
        sendSOSWithLocation(source);
        
        updateNotification("EMERGENCY ACTIVE", "Tap to mark yourself as SAFE");
        Toast.makeText(this, "SOS Triggered via " + source, Toast.LENGTH_LONG).show();
    }

    private void startFallbackCall() {
        Intent intent = new Intent(this, FallbackCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void stopEmergencyMode() {
        isEmergencyActive = false;
        
        // Stop Video Service
        stopService(new Intent(this, EmergencyVideoService.class));
        
        // Send "I am Safe" SMS
        sendSafeMessage();
        
        updateNotification("Sahayak Safety Active", "Shake or Say 'Emergency' to trigger SOS");
        Toast.makeText(this, "SOS Cancelled. 'I Am Safe' messages sent.", Toast.LENGTH_LONG).show();
    }

    private void sendSafeMessage() {
        List<String> contacts = mContactManager.getContacts();
        if (!contacts.isEmpty()) {
            SMSHelper.sendEmergencySMS(contacts, "SAFE");
        }
    }

    private void startEmergencyVideoService() {
        Intent videoIntent = new Intent(this, EmergencyVideoService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, videoIntent);
        } else {
            startService(videoIntent);
        }
    }

    private void sendSOSWithLocation(String source) {
        mLocationHelper.getLastLocation(this, location -> {
            String locationUrl = "https://maps.google.com/?q=Unknown";
            if (location != null) {
                locationUrl = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
            }
            
            List<String> contacts = mContactManager.getContacts();
            if (!contacts.isEmpty()) {
                SMSHelper.sendEmergencySMS(contacts, locationUrl);
            }
        });
    }

    private void updateNotification(String title, String text) {
        createNotificationChannel();
        
        Intent stopIntent = new Intent(this, EmergencyService.class);
        stopIntent.setAction("STOP_SOS");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (isEmergencyActive) {
            builder.addAction(android.R.drawable.ic_menu_save, "I AM SAFE", stopPendingIntent);
            builder.setColor(ContextCompat.getColor(this, R.color.primary_red));
        }

        startForeground(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Emergency Service Channel", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(mShakeDetector);
        mSensorManager.unregisterListener(mFallDetector);
        if (mVoiceSOSManager != null) {
            mVoiceSOSManager.stopListening();
            mVoiceSOSManager.destroy();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
