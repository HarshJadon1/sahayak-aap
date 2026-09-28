package com.sahayak.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class LockScreenActivity extends AppCompatActivity {

    private LocationHelper locationHelper;
    private ContactManager contactManager;
    private SharedPreferences medicalPrefs;
    private LinearLayout contactsLayout;

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
        medicalPrefs = getSharedPreferences("SahayakMedicalPrefs", Context.MODE_PRIVATE);
        contactsLayout = findViewById(R.id.layout_lock_contacts);

        displayMedicalInfo();
        displayContacts();

        FloatingActionButton fabSos = findViewById(R.id.fab_sos);
        fabSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerSOS();
            }
        });
    }

    private void displayMedicalInfo() {
        TextView tvBlood = findViewById(R.id.tv_lock_blood_group);
        TextView tvAllergies = findViewById(R.id.tv_lock_allergies);
        TextView tvConditions = findViewById(R.id.tv_lock_conditions);
        TextView tvNotes = findViewById(R.id.tv_lock_notes);

        tvBlood.setText("Blood Group: " + medicalPrefs.getString("blood_group", "--"));
        tvAllergies.setText("Allergies: " + medicalPrefs.getString("allergies", "None"));
        tvConditions.setText("Conditions: " + medicalPrefs.getString("conditions", "None"));
        tvNotes.setText("Emergency Notes: " + medicalPrefs.getString("notes", "No notes provided"));
    }

    private void displayContacts() {
        contactsLayout.removeAllViews();
        List<String> contacts = contactManager.getContacts();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (final String contactData : contacts) {
            String[] parts = contactData.split(":");
            String name;
            final String phone;
            if (parts.length > 1) {
                name = parts[0];
                phone = parts[1];
            } else {
                name = "Emergency Contact";
                phone = contactData;
            }
            String relation = parts.length > 2 ? parts[2] : "Emergency Contact";

            View contactView = inflater.inflate(R.layout.item_lock_contact, contactsLayout, false);
            
            TextView tvName = contactView.findViewById(R.id.tv_contact_name);
            TextView tvPhone = contactView.findViewById(R.id.tv_contact_phone);
            TextView tvRelation = contactView.findViewById(R.id.tv_contact_relation);
            View btnCall = contactView.findViewById(R.id.btn_call_contact);

            tvName.setText(name);
            tvPhone.setText(phone);
            tvRelation.setText(relation);

            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makeCall(phone);
                }
            });

            contactsLayout.addView(contactView);
        }
    }

    private void makeCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show();
        }
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
                    
                    // Extract only phone numbers if they are in CSV format
                    java.util.List<String> phoneNumbers = new java.util.ArrayList<>();
                    for(String c : contacts) {
                        String[] p = c.split(":");
                        phoneNumbers.add(p.length > 1 ? p[1] : c);
                    }

                    if (phoneNumbers.isEmpty()) {
                        Toast.makeText(LockScreenActivity.this, "No emergency contacts found!", Toast.LENGTH_SHORT).show();
                    } else {
                        SMSHelper.sendEmergencySMS(phoneNumbers, url);
                        Toast.makeText(LockScreenActivity.this, "Emergency SOS Sent!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LockScreenActivity.this, "Unable to get GPS location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
