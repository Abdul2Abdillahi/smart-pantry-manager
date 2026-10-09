package com.AbdulAbdillahi.smartpantrymanager.UI;

/** Small text helpers shared by several screens, so each formatting rule lives in one place. */
final class TextFormat {

    private TextFormat() {
        // Utility class: never instantiated
    }

    /** "pasta" -> "Pasta" */
    static String capitalise(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /** Shows 6.0 as "6", but keeps 0.5 as "0.5". */
    static String quantity(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}