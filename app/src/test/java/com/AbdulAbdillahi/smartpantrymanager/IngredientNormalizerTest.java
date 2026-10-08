package com.AbdulAbdillahi.smartpantrymanager;

import static org.junit.Assert.assertEquals;

import com.AbdulAbdillahi.smartpantrymanager.logic.IngredientNormalizer;

import org.junit.Test;

/** Checks that everyday variations of a name produce the same matching key. */
public class IngredientNormalizerTest {

    private static void assertKey(String expected, String input) {
        assertEquals(expected, IngredientNormalizer.normalize(input));
    }

    @Test public void plural_oes() { assertKey("tomato", "Tomatoes"); }
    @Test public void plural_s() { assertKey("egg", "EGGS"); }
    @Test public void plural_ies() { assertKey("berry", "Berries"); }
    @Test public void plural_ches() { assertKey("peach", "Peaches"); }
    @Test public void extraSpaces() { assertKey("egg", "   eggs  "); }
    @Test public void descriptorRemoved() { assertKey("tomato", "Fresh tomatoes"); }
    @Test public void onlyLastWordSingularised() { assertKey("green pepper", "Green Peppers"); }
    @Test public void punctuationBecomesSpace() { assertKey("stir fry sauce", "Stir-fry sauce"); }
    @Test public void aliasCheddarCheese() { assertKey("cheddar", "Cheddar Cheese"); }
    @Test public void aliasSouthAfricanSpelling() { assertKey("maize meal", "Mealie meal"); }
    @Test public void notPluralWordUnchanged() { assertKey("hummus", "Hummus"); }
    @Test public void nullBecomesEmpty() { assertKey("", null); }
}