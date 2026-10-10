package com.AbdulAbdillahi.smartpantrymanager.logic;

import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;
import com.AbdulAbdillahi.smartpantrymanager.model.Recipe;
import com.AbdulAbdillahi.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule (brief section 2.3):
 * a recipe is suggested ONLY if every ingredient it needs is in the pantry
 * in at least the required quantity. One missing or short ingredient = not suggested.
 * Pure Java (no Android code), so it is fully unit tested.
 */
public final class RecipeMatcher {

    /** The outcome for one recipe: which ingredients (if any) the pantry can't cover. */
    public static final class MatchResult {
        private final Recipe recipe;
        private final List<RecipeIngredient> missing;

        MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public List<RecipeIngredient> getMissing() {
            return Collections.unmodifiableList(missing);
        }

        /** True only if nothing is missing AND the recipe actually has ingredients. */
        public boolean canMake() {
            return missing.isEmpty() && !recipe.getIngredients().isEmpty();
        }
    }

    private RecipeMatcher() {
        // Utility class: never instantiated
    }

    /** Checks every recipe against the pantry and reports what each one is missing. */
    public static List<MatchResult> evaluate(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, PantryItem> pantryByKey = indexPantry(pantry);
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            results.add(new MatchResult(recipe, findMissing(recipe, pantryByKey)));
        }
        return results;
    }

    /** STRICT suggestions: only recipes where every single ingredient is covered. */
    public static List<Recipe> suggest(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> suggested = new ArrayList<>();
        for (MatchResult result : evaluate(recipes, pantry)) {
            if (result.canMake()) {
                suggested.add(result.getRecipe());
            }
        }
        return suggested;
    }
    /**
     * BONUS: recipes missing exactly ONE ingredient. Kept completely separate
     * from suggest(), so these can never appear in the strict suggestions list.
     */
    public static List<MatchResult> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> results = new ArrayList<>();
        for (MatchResult result : evaluate(recipes, pantry)) {
            if (result.getMissing().size() == 1) {
                results.add(result);
            }
        }
        return results;
    }
    /** Files each pantry item under its matching key, e.g. "Tomatoes" under "tomato". */
    private static Map<String, PantryItem> indexPantry(List<PantryItem> pantry) {
        Map<String, PantryItem> byKey = new HashMap<>();
        for (PantryItem item : pantry) {
            byKey.put(IngredientNormalizer.normalize(item.getName()), item);
        }
        return byKey;
    }

    /** Every ingredient the pantry doesn't have, or doesn't have enough of. */
    private static List<RecipeIngredient> findMissing(Recipe recipe,
                                                      Map<String, PantryItem> pantryByKey) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.getIngredients()) {
            PantryItem have = pantryByKey.get(IngredientNormalizer.normalize(needed.getName()));
            boolean covered = have != null && UnitConverter.isEnough(
                    have.getQuantity(), have.getUnit(),
                    needed.getQuantity(), needed.getUnit());
            if (!covered) {
                missing.add(needed);
            }
        }
        return missing;
    }
}