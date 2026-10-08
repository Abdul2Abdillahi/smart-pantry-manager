package com.AbdulAbdillahi.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.AbdulAbdillahi.smartpantrymanager.logic.RecipeMatcher;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;
import com.AbdulAbdillahi.smartpantrymanager.model.Recipe;
import com.AbdulAbdillahi.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests the strict-matching rule from section 2.3 of the brief.
 * Each test name describes the situation and the expected result.
 */
public class RecipeMatcherTest {

    // ---------- helpers: build test data in one line ----------

    /** Ingredients written as "name|quantity|unit", like the seeder. */
    private static Recipe recipe(String name, String... ingredients) {
        Recipe recipe = new Recipe(1, name, "Test steps", 10, 1);
        for (String entry : ingredients) {
            String[] parts = entry.split("\\|");
            recipe.addIngredient(new RecipeIngredient(
                    parts[0], Double.parseDouble(parts[1]), parts[2]));
        }
        return recipe;
    }

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    private static List<Recipe> suggest(Recipe recipe, PantryItem... pantry) {
        return RecipeMatcher.suggest(Collections.singletonList(recipe), Arrays.asList(pantry));
    }

    // ---------- the core rule ----------

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        Recipe toastie = recipe("Cheese Toastie", "bread|2|pcs", "cheddar|50|g", "butter|10|g");
        List<Recipe> result = suggest(toastie,
                item("Bread", 4, "pcs"), item("Cheddar", 200, "g"), item("Butter", 250, "g"));
        assertEquals(1, result.size());
    }

    @Test
    public void fourOfFiveIngredients_recipeIsNotSuggested() {
        // The exact example from the brief: 5 needed, 4 in the pantry -> must NOT appear
        Recipe pasta = recipe("Garlic Butter Pasta",
                "pasta|200|g", "butter|50|g", "garlic|3|pcs", "cheddar|50|g", "onion|1|pcs");
        List<Recipe> result = suggest(pasta,
                item("Pasta", 500, "g"), item("Butter", 250, "g"),
                item("Garlic", 5, "pcs"), item("Cheddar", 200, "g")); // no onion
        assertTrue(result.isEmpty());
    }

    @Test
    public void notEnoughQuantity_recipeIsNotSuggested() {
        Recipe omelette = recipe("Omelette", "eggs|3|pcs");
        assertTrue(suggest(omelette, item("Eggs", 2, "pcs")).isEmpty());
    }

    @Test
    public void exactlyEnough_recipeIsSuggested() {
        Recipe omelette = recipe("Omelette", "eggs|3|pcs");
        assertEquals(1, suggest(omelette, item("Eggs", 3, "pcs")).size());
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        Recipe toastie = recipe("Cheese Toastie", "bread|2|pcs");
        assertTrue(suggest(toastie).isEmpty());
    }

    @Test
    public void recipeWithNoIngredients_isNotSuggested() {
        assertTrue(suggest(recipe("Empty"), item("Bread", 4, "pcs")).isEmpty());
    }

    // ---------- real-world messiness ----------

    @Test
    public void pluralAndCapitals_stillMatch() {
        // Pantry says "Tomatoes", recipe says "tomato"
        Recipe salad = recipe("Salad", "tomato|1|pcs");
        assertEquals(1, suggest(salad, item("Tomatoes", 4, "pcs")).size());
    }

    @Test
    public void litresCoverMillilitres() {
        Recipe porridge = recipe("Porridge", "milk|250|ml");
        assertEquals(1, suggest(porridge, item("Milk", 1, "L")).size());
    }

    @Test
    public void kilogramsCoverGrams() {
        Recipe pasta = recipe("Pasta", "pasta|200|g");
        assertEquals(1, suggest(pasta, item("Pasta", 0.5, "kg")).size());
    }

    @Test
    public void tablespoonsCompareWithMillilitres() {
        Recipe stirFry = recipe("Stir-fry", "oil|2|tbsp");          // 2 tbsp = 30 ml
        assertEquals(1, suggest(stirFry, item("Oil", 500, "ml")).size());
        assertTrue(suggest(stirFry, item("Oil", 20, "ml")).isEmpty());
    }

    @Test
    public void incompatibleUnits_doNotMatch() {
        // We can't know how much one onion weighs, so grams can't cover "pieces"
        Recipe soup = recipe("Soup", "onion|1|pcs");
        assertTrue(suggest(soup, item("Onions", 300, "g")).isEmpty());
    }

    @Test
    public void similarNames_doNotFalselyMatch() {
        // "Rice vinegar" contains the word "rice" but is NOT rice
        Recipe rice = recipe("Plain Rice", "rice|200|g");
        assertTrue(suggest(rice, item("Rice vinegar", 500, "ml")).isEmpty());
    }

    // ---------- the missing list (used by "Almost There") ----------

    @Test
    public void evaluate_reportsExactlyWhatIsMissing() {
        Recipe pasta = recipe("Garlic Butter Pasta", "pasta|200|g", "garlic|3|pcs");
        RecipeMatcher.MatchResult result = RecipeMatcher.evaluate(
                Collections.singletonList(pasta),
                Collections.singletonList(item("Pasta", 500, "g"))).get(0);
        assertEquals(1, result.getMissing().size());
        assertEquals("garlic", result.getMissing().get(0).getName());
    }
}
