package com.sahayak.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.material.button.MaterialButton;

public class FallbackCallActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private MaterialButton btnCancel;
    private CountDownTimer countDownTimer;
    private static final int CALL_PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show over lock screen
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        
        setContentView(R.layout.activity_fallback_call);

        tvCountdown = findViewById(R.id.tv_fallback_countdown);
        btnCancel = findViewById(R.id.btn_cancel_call);

        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                makeEmergencyCall();
            }
        }.start();

        btnCancel.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            finish();
        });
    }

    private void makeEmergencyCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:112"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
            return;
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCall();
            } else {
                Toast.makeText(this, "Call permission denied. Dial 112 manually.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
