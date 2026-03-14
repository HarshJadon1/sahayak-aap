package com.sahayak.app;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AccidentAlertActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private MaterialButton btnCancel;
    private CountDownTimer countDownTimer;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ensure it shows over lock screen
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        
        setContentView(R.layout.activity_accident_alert);

        tvCountdown = findViewById(R.id.tv_countdown);
        btnCancel = findViewById(R.id.btn_cancel_sos);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startAlertEffects();

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                triggerSOS();
            }
        }.start();

        btnCancel.setOnClickListener(v -> {
            stopAlertEffects();
            countDownTimer.cancel();
            finish();
        });
    }

    private void startAlertEffects() {
        // Vibrate
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 200}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 500, 200}, 0);
            }
        }
        
        // Play Alert Sound
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopAlertEffects() {
        if (vibrator != null) vibrator.cancel();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void triggerSOS() {
        stopAlertEffects();
        
        // Trigger the emergency mode in the service
        Intent serviceIntent = new Intent(this, EmergencyService.class);
        serviceIntent.setAction("TRIGGER_SOS");
        startService(serviceIntent);
        
        finish();
    }

    @Override
    protected void onDestroy() {
        stopAlertEffects();
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
