package com.AbdulAbdillahi.smartpantrymanager.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The user's settings, stored with SharedPreferences (simple key -> value storage,
 * ideal for a few small values). Every key and default lives here, in one place.
 */
public final class AppSettings {

    private static final String PREFS_FILE = "smart_pantry_settings";
    private static final String KEY_WARNINGS_ENABLED = "expiry_warnings_enabled";
    private static final String KEY_SOON_DAYS = "expiring_soon_days";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    public static final int MIN_SOON_DAYS = 1;
    public static final int MAX_SOON_DAYS = 7;
    private static final int DEFAULT_SOON_DAYS = 3;
    private static final String DEFAULT_UNIT = "pcs";

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    public boolean areWarningsEnabled() {
        return prefs.getBoolean(KEY_WARNINGS_ENABLED, true);
    }

    public void setWarningsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WARNINGS_ENABLED, enabled).apply();
    }

    /** Days before expiry that count as "use soon", always kept within 1–7. */
    public int getSoonDays() {
        int days = prefs.getInt(KEY_SOON_DAYS, DEFAULT_SOON_DAYS);
        return Math.max(MIN_SOON_DAYS, Math.min(MAX_SOON_DAYS, days));
    }

    public void setSoonDays(int days) {
        prefs.edit().putInt(KEY_SOON_DAYS, days).apply();
    }

    /**
     * The window the app should actually use. 0 when warnings are off, so only
     * expired items and food expiring today are flagged (those always matter).
     */
    public int effectiveSoonDays() {
        return areWarningsEnabled() ? getSoonDays() : 0;
    }

    public String getDefaultUnit() {
        return prefs.getString(KEY_DEFAULT_UNIT, DEFAULT_UNIT);
    }

    public void setDefaultUnit(String unit) {
        prefs.edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}