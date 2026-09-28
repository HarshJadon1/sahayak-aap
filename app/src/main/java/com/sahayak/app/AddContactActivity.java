package com.sahayak.app;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;

public class AddContactActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone;
    private MaterialButton btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        etName = findViewById(R.id.et_guardian_name);
        etPhone = findViewById(R.id.et_guardian_phone);
        btnVerify = findViewById(R.id.btn_verify_contact);

        btnVerify.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.length() < 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            generateAndSendOTP(name, phone);
        });
    }

    private void generateAndSendOTP(String name, String phone) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        String message = "Sahayak Verification Code: " + otp + ". Enter this code in the app to confirm you are an emergency contact.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
            
            Intent intent = new Intent(this, VerifyOtpActivity.class);
            intent.putExtra("guardian_name", name);
            intent.putExtra("guardian_phone", phone);
            intent.putExtra("generated_otp", otp);
            startActivity(intent);
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS. Check permissions.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
