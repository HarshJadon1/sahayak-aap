package com.sahayak.app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class FallDetector implements SensorEventListener {

    private static final float FREE_FALL_THRESHOLD = 2.0f; // m/s^2
    private static final float IMPACT_THRESHOLD = 18.0f;    // m/s^2
    private static final long IMPACT_WINDOW_MS = 1000;     // Time to wait for impact after free fall

    private OnFallListener mListener;
    private boolean mIsInFreeFall = false;
    private long mFreeFallTimestamp = 0;

    public interface OnFallListener {
        void onFallDetected();
    }

    public void setOnFallListener(OnFallListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);

            long currentTime = System.currentTimeMillis();

            // 1. Detect Free Fall
            if (acceleration < FREE_FALL_THRESHOLD) {
                mIsInFreeFall = true;
                mFreeFallTimestamp = currentTime;
            }

            // 2. Detect Impact if we were recently in free fall
            if (mIsInFreeFall) {
                if (currentTime - mFreeFallTimestamp > IMPACT_WINDOW_MS) {
                    mIsInFreeFall = false; // Window expired
                } else if (acceleration > IMPACT_THRESHOLD) {
                    if (mListener != null) {
                        mListener.onFallDetected();
                    }
                    mIsInFreeFall = false; // Reset
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
