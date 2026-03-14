package com.sahayak.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MedicalInfoActivity extends AppCompatActivity {
    private EditText etBloodGroup, etAllergies, etConditions, etNotes;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "SahayakMedicalPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_info);

        etBloodGroup = findViewById(R.id.et_blood_group);
        etAllergies = findViewById(R.id.et_allergies);
        etConditions = findViewById(R.id.et_conditions);
        etNotes = findViewById(R.id.et_notes);

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadMedicalInfo();

        findViewById(R.id.btn_save_medical).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMedicalInfo();
            }
        });
    }

    private void loadMedicalInfo() {
        etBloodGroup.setText(prefs.getString("blood_group", ""));
        etAllergies.setText(prefs.getString("allergies", ""));
        etConditions.setText(prefs.getString("conditions", ""));
        etNotes.setText(prefs.getString("notes", ""));
    }

    private void saveMedicalInfo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("blood_group", etBloodGroup.getText().toString());
        editor.putString("allergies", etAllergies.getText().toString());
        editor.putString("conditions", etConditions.getText().toString());
        editor.putString("notes", etNotes.getText().toString());
        editor.apply();
        Toast.makeText(this, "Information Saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
