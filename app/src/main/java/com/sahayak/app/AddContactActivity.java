package com.sahayak.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;

public class AddContactActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private TextInputEditText etName, etPhone;
    private MaterialButton btnVerify;
    private String pendingName, pendingPhone;
    private String editOriginal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        etName = findViewById(R.id.et_guardian_name);
        etPhone = findViewById(R.id.et_guardian_phone);
        btnVerify = findViewById(R.id.btn_verify_contact);

        if (getIntent().hasExtra("edit_number")) {
            editOriginal = getIntent().getStringExtra("edit_number");
            if (editOriginal != null) {
                if (editOriginal.contains(":")) {
                    String[] parts = editOriginal.split(":", 2);
                    etName.setText(parts[0]);
                    etPhone.setText(parts[1]);
                } else {
                    etPhone.setText(editOriginal);
                }
            }
        }

        btnVerify.setOnClickListener(v -> {
            pendingName = etName.getText().toString().trim();
            pendingPhone = etPhone.getText().toString().trim();

            if (pendingName.isEmpty() || pendingPhone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pendingPhone.length() < 10) {
                Toast.makeText(this, "Enter a valid 10-digit number", Toast.LENGTH_SHORT).show();
                return;
            }

            checkSmsPermission();
        });
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            generateAndSendOTP(pendingName, pendingPhone);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndSendOTP(pendingName, pendingPhone);
            } else {
                Toast.makeText(this, "SMS Permission is required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateAndSendOTP(String name, String phone) {
        // Simple 6 digit OTP
        Random random = new Random();
        int otpInt = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpInt);
        
        // Simple Message
        String message = "Sahayak Code: " + otp;

        // Number handling
        String finalPhone = phone;
        if (phone.length() == 10 && !phone.startsWith("+")) {
            finalPhone = "+91" + phone;
        }

        try {
            // Using the most basic SmsManager (same as your SMSHelper)
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(finalPhone, null, message, null, null);
            
            Log.d("OTP_DEBUG", "Sent " + otp + " to " + finalPhone);
            Toast.makeText(this, "OTP Sent to " + finalPhone, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, VerifyOtpActivity.class);
            intent.putExtra("guardian_name", name);
            intent.putExtra("guardian_phone", phone);
            intent.putExtra("generated_otp", otp);
            if (editOriginal != null) {
                intent.putExtra("edit_original", editOriginal);
            }
            startActivity(intent);
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("OTP_ERROR", "Error sending SMS", e);
        }
    }
}
