package com.sahayak.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceSOSManager {
    private static final String TAG = "VoiceSOSManager";
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private Context context;
    private OnVoiceCommandListener listener;
    private boolean isListening = false;

    public interface OnVoiceCommandListener {
        void onEmergencyCommandDetected();
    }

    public VoiceSOSManager(Context context, OnVoiceCommandListener listener) {
        this.context = context;
        this.listener = listener;
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // For continuous listening, we need to handle the results and start again

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
            }

            @Override
            public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO: message = "Audio recording error"; break;
                    case SpeechRecognizer.ERROR_CLIENT: message = "Client side error"; break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Insufficient permissions"; break;
                    case SpeechRecognizer.ERROR_NETWORK: message = "Network error"; break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "Network timeout"; break;
                    case SpeechRecognizer.ERROR_NO_MATCH: message = "No match found"; break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: message = "Recognizer busy"; break;
                    case SpeechRecognizer.ERROR_SERVER: message = "Server error"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No speech input"; break;
                    default: message = "Unknown error"; break;
                }
                Log.e(TAG, "Speech Error: " + message);
                
                // Restart listening if it was supposed to be listening
                if (isListening) {
                    startListening();
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String match : matches) {
                        Log.d(TAG, "Detected: " + match);
                        if (isEmergencyPhrase(match)) {
                            if (listener != null) {
                                listener.onEmergencyCommandDetected();
                            }
                            break;
                        }
                    }
                }
                
                // Restart listening for continuous detection
                if (isListening) {
                    startListening();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private boolean isEmergencyPhrase(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("help sahayak") || 
               lowerText.contains("emergency") || 
               lowerText.contains("bachao") ||
               lowerText.contains("save me");
    }

    public void startListening() {
        isListening = true;
        try {
            speechRecognizer.startListening(speechRecognizerIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start listening", e);
        }
    }

    public void stopListening() {
        isListening = false;
        speechRecognizer.stopListening();
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
