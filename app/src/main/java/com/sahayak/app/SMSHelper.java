package com.sahayak.app;

import android.telephony.SmsManager;
import java.util.List;

public class SMSHelper {
    public static void sendEmergencySMS(List<String> contacts, String locationUrl) {
        String message;
        if ("SAFE".equals(locationUrl)) {
            message = "Update from Sahayak App: The previous emergency alert was sent by mistake. I am safe and no help is needed.";
        } else {
            message = "Emergency Alert!\n\nI need help.\n\nMy location:\n" + locationUrl + "\n\nSent via Sahayak";
        }

        SmsManager smsManager = SmsManager.getDefault();
        java.util.ArrayList<String> parts = smsManager.divideMessage(message);
        for (String contact : contacts) {
            try {
                if (contact == null || contact.trim().isEmpty()) continue;
                String phoneNumber = contact.contains(":") ? contact.split(":", 2)[1].trim() : contact.trim();
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
