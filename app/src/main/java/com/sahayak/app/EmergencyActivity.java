package com.sahayak.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class EmergencyActivity extends AppCompatActivity {
    private TextView tvBlood, tvAllergies, tvConditions, tvNotes;
    private ListView lvContacts;
    private SharedPreferences medicalPrefs;
    private ContactManager contactManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity show over lock screen
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        
        setContentView(R.layout.activity_emergency);

        tvBlood = findViewById(R.id.tv_emergency_blood);
        tvAllergies = findViewById(R.id.tv_emergency_allergies);
        tvConditions = findViewById(R.id.tv_emergency_conditions);
        tvNotes = findViewById(R.id.tv_emergency_notes);
        lvContacts = findViewById(R.id.lv_emergency_contacts);

        medicalPrefs = getSharedPreferences("SahayakMedicalPrefs", Context.MODE_PRIVATE);
        contactManager = new ContactManager(this);

        displayInfo();
    }

    private void displayInfo() {
        tvBlood.setText(medicalPrefs.getString("blood_group", "Not Provided"));
        tvAllergies.setText(medicalPrefs.getString("allergies", "Not Provided"));
        tvConditions.setText(medicalPrefs.getString("conditions", "Not Provided"));
        tvNotes.setText(medicalPrefs.getString("notes", "Not Provided"));

        List<String> contacts = contactManager.getContacts();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contacts);
        lvContacts.setAdapter(adapter);
    }
}
