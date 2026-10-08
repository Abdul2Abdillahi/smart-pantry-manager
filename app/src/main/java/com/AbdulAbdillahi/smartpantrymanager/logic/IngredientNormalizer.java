package com.AbdulAbdillahi.smartpantrymanager.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns an ingredient name into a standard "matching key", so small
 * real-world differences don't stop a match:
 *   "Tomatoes", " tomato ", "Fresh tomatoes" -> "tomato"
 *   "Green Peppers"                         -> "green pepper"
 *   "Mealie meal"                           -> "maize meal"
 * The same rules run on pantry items AND recipe ingredients, so both sides
 * always end up with the same key. Pure Java, so it can be unit tested.
 */
public final class IngredientNormalizer {

    /** Describing words that don't change what the ingredient is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "ripe", "whole", "raw"));

    /** Words that end in "s" but aren't plurals. */
    private static final Set<String> NOT_PLURAL = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus"));

    /** Plurals the general rules would get wrong. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    /** Different names for the same ingredient (checked after singularising). */
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("chillies", "chilli");
        IRREGULAR.put("cookies", "cookie");
        IRREGULAR.put("brownies", "brownie");
        IRREGULAR.put("pies", "pie");

        ALIASES.put("cheddar cheese", "cheddar");
        ALIASES.put("mealie meal", "maize meal");   // common South African spelling
        ALIASES.put("minced beef", "beef mince");
    }

    private IngredientNormalizer() {
        // Utility class: never instantiated
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }

        // 1. Lowercase, and turn anything that isn't a letter into a space ("Stir-fry" -> "stir fry")
        String cleaned = name.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}]+", " ").trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        // 2. Drop describing words and singularise the last word (the ingredient itself)
        String[] words = cleaned.split(" ");
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            boolean isLast = (i == words.length - 1);
            if (!isLast && DESCRIPTORS.contains(words[i])) {
                continue;
            }
            if (key.length() > 0) {
                key.append(' ');
            }
            key.append(isLast ? singular(words[i]) : words[i]);
        }

        // 3. Map known alternative names to one standard name
        String result = key.toString();
        String alias = ALIASES.get(result);
        return alias != null ? alias : result;
    }

    /** Turns one English word into its singular form using simple rules. */
    static String singular(String word) {
        if (word.length() <= 3 || NOT_PLURAL.contains(word)) {
            return word;
        }
        String irregular = IRREGULAR.get(word);
        if (irregular != null) {
            return irregular;
        }
        if (word.endsWith("ies")) {                          // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes")) {                          // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ches") || word.endsWith("shes")   // peaches -> peach
                || word.endsWith("xes") || word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                     // not plurals
        }
        if (word.endsWith("s")) {                            // eggs -> egg
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}