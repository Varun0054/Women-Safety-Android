package com.example.womensafe;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class PowerButtonAccessibilityService extends AccessibilityService {

    public static final String ACTION_SCREEN_EVENT = "com.example.womensafe.SCREEN_EVENT";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We are only interested in window state changes to infer power button presses.
        // We do not read content or gestures.
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Intent intent = new Intent(ACTION_SCREEN_EVENT);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onInterrupt() {
        // This method is called when the service is interrupted, e.g., by another service.
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // We can configure the service here if needed, but we rely on the XML config.
    }
}
