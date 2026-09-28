package com.sahayak.app;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class SahayakAppUnitTest {

    @Test
    public void testContactParsing_nameAndPhone() {
        String contactData = "Priya:+919876543210";
        String[] parts = contactData.split(":", 2);
        assertEquals(2, parts.length);
        assertEquals("Priya", parts[0]);
        assertEquals("+919876543210", parts[1]);
    }

    @Test
    public void testContactParsing_plainPhoneNumber() {
        String contactData = "9876543210";
        String name;
        String phone;
        if (contactData.contains(":")) {
            String[] parts = contactData.split(":", 2);
            name = parts[0];
            phone = parts[1];
        } else {
            name = "Emergency Contact";
            phone = contactData;
        }
        assertEquals("Emergency Contact", name);
        assertEquals("9876543210", phone);
    }

    @Test
    public void testPhoneNumberSanitization() {
        String rawPhone = "9876543210";
        String finalPhone = rawPhone;
        if (rawPhone.length() == 10 && !rawPhone.startsWith("+")) {
            finalPhone = "+91" + rawPhone;
        }
        assertEquals("+919876543210", finalPhone);

        // When already formatted with country code
        String alreadyFormatted = "+919876543210";
        if (alreadyFormatted.length() == 10 && !alreadyFormatted.startsWith("+")) {
            alreadyFormatted = "+91" + alreadyFormatted;
        }
        assertEquals("+919876543210", alreadyFormatted);
    }

    @Test
    public void testOtpGeneration_isSixDigits() {
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 50; i++) {
            int otpInt = 100000 + random.nextInt(900000);
            String otp = String.valueOf(otpInt);
            assertEquals(6, otp.length());
            assertTrue(otpInt >= 100000 && otpInt <= 999999);
        }
    }

    @Test
    public void testOtpVerification() {
        String generatedOtp = "458921";
        String userEnteredCorrect = "458921";
        String userEnteredWrong = "123456";

        assertTrue(userEnteredCorrect.equals(generatedOtp));
        assertFalse(userEnteredWrong.equals(generatedOtp));
    }

    @Test
    public void testSmsEmergencyMessageFormat() {
        String locationUrl = "https://maps.google.com/?q=28.7041,77.1025";
        String message = "Emergency Alert!\n\nI need help.\n\nMy location:\n" + locationUrl + "\n\nSent via Sahayak";

        assertTrue(message.contains("Emergency Alert!"));
        assertTrue(message.contains(locationUrl));
        assertTrue(message.contains("Sent via Sahayak"));
    }

    @Test
    public void testSmsSafeMessageFormat() {
        String locationUrl = "SAFE";
        String message;
        if ("SAFE".equals(locationUrl)) {
            message = "Update from Sahayak App: The previous emergency alert was sent by mistake. I am safe and no help is needed.";
        } else {
            message = "Emergency Alert!\n\nI need help.\n\nMy location:\n" + locationUrl + "\n\nSent via Sahayak";
        }

        assertTrue(message.contains("I am safe and no help is needed."));
    }

    @Test
    public void testExtractPhoneNumbersFromContactList() {
        List<String> rawContacts = new ArrayList<>();
        rawContacts.add("Mom:+919876543210");
        rawContacts.add("9876543211");
        rawContacts.add("Dad:+919876543212:Parent");

        List<String> extractedPhones = new ArrayList<>();
        for (String c : rawContacts) {
            String phone = c.contains(":") ? c.split(":")[1].trim() : c.trim();
            extractedPhones.add(phone);
        }

        assertEquals(3, extractedPhones.size());
        assertEquals("+919876543210", extractedPhones.get(0));
        assertEquals("9876543211", extractedPhones.get(1));
        assertEquals("+919876543212", extractedPhones.get(2));
    }

    @Test
    public void testVoiceEmergencyKeywordMatching() {
        String[] keywords = {"help sahayak", "emergency", "bachao", "save me"};
        
        String input1 = "Please bachao mujhe!";
        boolean match1 = false;
        for (String kw : keywords) {
            if (input1.toLowerCase().contains(kw)) {
                match1 = true;
                break;
            }
        }
        assertTrue(match1);

        String input2 = "Where is the nearest restaurant?";
        boolean match2 = false;
        for (String kw : keywords) {
            if (input2.toLowerCase().contains(kw)) {
                match2 = true;
                break;
            }
        }
        assertFalse(match2);
    }
}
