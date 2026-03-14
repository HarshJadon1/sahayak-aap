package com.sahayak.app;

import android.telephony.SmsManager;
import java.util.List;

public class SMSHelper {
    public static void sendEmergencySMS(List<String> contacts, String locationUrl) {
        String message = "Emergency! I need help.\n\nMy current location:\n" + locationUrl + "\n\nSent via Sahayak App";
        SmsManager smsManager = SmsManager.getDefault();
        for (String contact : contacts) {
            try {
                smsManager.sendTextMessage(contact, null, message, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
