package dev.xox.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class ShortcutAccessibilityService extends AccessibilityService {
    private int firstKey = KeyEvent.KEYCODE_VOLUME_UP;
    private long firstTime = 0;
    private long lastLaunch = 0;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }

    @Override public boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() != 0) return false;
        int mode = ShortcutPrefs.getMode(this);
        if (mode == 2) return false;
        int expectedFirst = mode == 1 ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP;
        int expectedSecond = mode == 1 ? KeyEvent.KEYCODE_VOLUME_UP : KeyEvent.KEYCODE_VOLUME_DOWN;
        int key = event.getKeyCode();
        long now = SystemClock.elapsedRealtime();
        if (key == expectedFirst) {
            firstKey = key; firstTime = now; return false;
        }
        if (key == expectedSecond && firstKey == expectedFirst && now - firstTime <= 900) {
            firstKey = 0;
            if (now - lastLaunch > 1400) {
                lastLaunch = now;
                Intent i = new Intent(this, AssistantActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try { startActivity(i); } catch (Exception ignored) { }
            }
            return true;
        }
        if (now - firstTime > 900) firstKey = 0;
        return false;
    }
}
