package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.app.Activity;
import android.content.Intent;

import com.AbdulAbdillahi.smartpantrymanager.MainActivity;
import com.AbdulAbdillahi.smartpantrymanager.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class BottomNav {

    private BottomNav() {
        // Utility class: never instantiated
    }

    /** Highlights this screen's tab and handles taps on the other tabs. */
    public static void setup(Activity activity, int currentItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(currentItemId); // set before the listener, so it doesn't trigger
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true; // already on this screen
            }
            if (id == R.id.nav_shelf) {
                open(activity, MainActivity.class);
            } else if (id == R.id.nav_recipes) {
                open(activity, SuggestedRecipesActivity.class);
            } else if (id == R.id.nav_settings) {
                open(activity, SettingsActivity.class);
            }
            return false; // this screen keeps its own tab highlighted
        });
    }

    /**
     * Opens a main screen. The Shelf always stays at the bottom, with at most one
     * other tab on top of it, so Back from Recipes or Settings always returns to
     * the Shelf, and copies of screens never pile up.
     */
    public static void open(Activity activity, Class<?> screen) {
        Intent intent = new Intent(activity, screen);
        if (screen == MainActivity.class) {
            // Go back to the existing Shelf, closing whatever tab was on top of it
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        } else {
            activity.startActivity(intent);
            if (!(activity instanceof MainActivity)) {
                activity.finish(); // swap one tab for another instead of stacking them
            }
        }
    }
}