package dev.xox.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class ShortcutAccessibilityService extends AccessibilityService {
    private static final long SEQUENCE_TIMEOUT_MS = 900;
    private static final long LAUNCH_COOLDOWN_MS = 1400;

    private int firstKey = 0;
    private long firstTime = 0;
    private long lastLaunch = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Xox does not inspect window contents or accessibility event data.
    }

    @Override
    public void onInterrupt() {
        resetSequence();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        resetSequence();
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() != 0) {
            return false;
        }

        int mode = ShortcutPrefs.getMode(this);
        if (mode == 2) {
            resetSequence();
            return false;
        }

        int expectedFirst = mode == 1
                ? KeyEvent.KEYCODE_VOLUME_DOWN
                : KeyEvent.KEYCODE_VOLUME_UP;
        int expectedSecond = mode == 1
                ? KeyEvent.KEYCODE_VOLUME_UP
                : KeyEvent.KEYCODE_VOLUME_DOWN;

        int key = event.getKeyCode();
        long now = SystemClock.elapsedRealtime();

        // Starting the sequence again also clears any stale partial sequence.
        if (key == expectedFirst) {
            firstKey = key;
            firstTime = now;
            return false; // Let the first volume press behave normally.
        }

        if (key == expectedSecond && firstKey == expectedFirst) {
            boolean inTime = now - firstTime <= SEQUENCE_TIMEOUT_MS;
            resetSequence();

            if (!inTime) {
                return false;
            }

            if (lastLaunch == 0 || now - lastLaunch >= LAUNCH_COOLDOWN_MS) {
                lastLaunch = now;
                Intent intent = new Intent(this, AssistantActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try {
                    startActivity(intent);
                } catch (RuntimeException ignored) {
                    // The OS may refuse an activity launch in some device states.
                }
            }
            return true;
        }

        // Any unrelated key breaks the sequence; it cannot accidentally complete later.
        resetSequence();
        return false;
    }

    private void resetSequence() {
        firstKey = 0;
        firstTime = 0;
    }
}
