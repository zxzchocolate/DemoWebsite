package dev.xox.assistant;

import android.content.Context;

final class ShortcutPrefs {
    private static final String FILE = "xox-settings";
    static int getMode(Context c) { return c.getSharedPreferences(FILE, 0).getInt("key_mode", 0); }
    static void setMode(Context c, int mode) { c.getSharedPreferences(FILE, 0).edit().putInt("key_mode", mode).apply(); }
    private ShortcutPrefs() {}
}
