package com.sahayak.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class LockScreenActivity extends AppCompatActivity {

    private LocationHelper locationHelper;
    private ContactManager contactManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup to show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.activity_lock_screen);

        locationHelper = new LocationHelper(this);
        contactManager = new ContactManager(this);

        FloatingActionButton fabSos = findViewById(R.id.fab_sos);
        fabSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerSOS();
            }
        });
    }

    private void triggerSOS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            
            Toast.makeText(this, "Location and SMS permissions required", Toast.LENGTH_LONG).show();
            return;
        }

        locationHelper.getLastLocation(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String url = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                    List<String> contacts = contactManager.getContacts();
                    
                    if (contacts.isEmpty()) {
                        Toast.makeText(LockScreenActivity.this, "No emergency contacts found!", Toast.LENGTH_SHORT).show();
                    } else {
                        SMSHelper.sendEmergencySMS(contacts, url);
                        Toast.makeText(LockScreenActivity.this, "Emergency SOS Sent!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LockScreenActivity.this, "Unable to get GPS location. Please ensure GPS is ON.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
