package com.sahayak.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private LocationHelper locationHelper;
    private ContactManager contactManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new LocationHelper(this);
        contactManager = new ContactManager(this);

        findViewById(R.id.btn_sos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndSendSOS();
            }
        });

        findViewById(R.id.btn_medical_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MedicalInfoActivity.class));
            }
        });

        findViewById(R.id.btn_contacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
            }
        });

        findViewById(R.id.btn_emergency_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EmergencyActivity.class));
            }
        });
    }

    private void checkPermissionsAndSendSOS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS
            }, PERMISSION_REQUEST_CODE);
        } else {
            sendSOS();
        }
    }

    private void sendSOS() {
        locationHelper.getLastLocation(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String url = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                    List<String> contacts = contactManager.getContacts();
                    if (contacts.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No emergency contacts added!", Toast.LENGTH_SHORT).show();
                    } else {
                        SMSHelper.sendEmergencySMS(contacts, url);
                        Toast.makeText(MainActivity.this, "SOS Sent!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSOS();
            } else {
                Toast.makeText(this, "Permissions required for SOS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
