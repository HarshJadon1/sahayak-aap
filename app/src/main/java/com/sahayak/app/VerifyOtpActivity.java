package com.sahayak.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;

public class VerifyOtpActivity extends AppCompatActivity {

    private TextInputEditText etOtp;
    private MaterialButton btnVerify, btnResend;
    private TextView tvTimer;
    private String guardianName, guardianPhone, generatedOtp;
    private CountDownTimer countDownTimer;
    private ContactManager contactManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        contactManager = new ContactManager(this);

        etOtp = findViewById(R.id.et_otp);
        btnVerify = findViewById(R.id.btn_verify_otp);
        btnResend = findViewById(R.id.btn_resend_otp);
        tvTimer = findViewById(R.id.tv_timer);

        guardianName = getIntent().getStringExtra("guardian_name");
        guardianPhone = getIntent().getStringExtra("guardian_phone");
        generatedOtp = getIntent().getStringExtra("generated_otp");
        final String editOriginal = getIntent().getStringExtra("edit_original");

        startTimer();

        btnVerify.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString().trim();
            if (enteredOtp.equals(generatedOtp)) {
                String contactEntry;
                if (guardianName != null && !guardianName.trim().isEmpty()) {
                    contactEntry = guardianName.trim() + ":" + guardianPhone.trim();
                } else {
                    contactEntry = guardianPhone.trim();
                }

                if (editOriginal != null) {
                    contactManager.updateContact(editOriginal, contactEntry);
                } else {
                    contactManager.addContact(contactEntry);
                }

                Toast.makeText(this, "Contact Verified and Saved", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(this, ContactsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        btnResend.setOnClickListener(v -> {
            resendOTP();
        });
    }

    private void startTimer() {
        btnResend.setVisibility(View.GONE);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("OTP expires in %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("OTP Expired");
                generatedOtp = "EXPIRED";
                btnResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void resendOTP() {
        generatedOtp = String.format("%06d", new Random().nextInt(1000000));
        String message = "Sahayak Verification Code: " + generatedOtp + ". Enter this code in the app to confirm you are an emergency contact.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(guardianPhone, null, message, null, null);
            Toast.makeText(this, "OTP Resent", Toast.LENGTH_SHORT).show();
            startTimer();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
