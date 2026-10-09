package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper;
import com.AbdulAbdillahi.smartpantrymanager.logic.IngredientNormalizer;
import com.AbdulAbdillahi.smartpantrymanager.logic.RecipeMatcher;
import com.AbdulAbdillahi.smartpantrymanager.logic.UnitConverter;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;
import com.AbdulAbdillahi.smartpantrymanager.model.Recipe;
import com.AbdulAbdillahi.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows one recipe in full: an ingredient checklist against the user's
 * pantry (✓ have it / ✗ missing), and the numbered method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra: id of the recipe to show. */
    public static final String EXTRA_RECIPE_ID =
            "com.AbdulAbdillahi.smartpantrymanager.EXTRA_RECIPE_ID";

    private DatabaseHelper db;
    private long recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = DatabaseHelper.getInstance(this);
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRecipe(); // re-check against the pantry every time, in case it changed
    }

    private void showRecipe() {
        Recipe recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            finish(); // opened with an id that doesn't exist: nothing to show
            return;
        }

        TextView title = findViewById(R.id.detailTitle);
        TextView meta = findViewById(R.id.detailMeta);
        TextView status = findViewById(R.id.detailStatus);
        title.setText(recipe.getName());
        meta.setText(getString(R.string.recipe_meta, recipe.getPrepMinutes(), recipe.getServings()));

        // Ask the matcher exactly which ingredients this pantry can't cover
        List<PantryItem> pantry = db.getAllPantryItems();
        RecipeMatcher.MatchResult result = RecipeMatcher.evaluate(
                Collections.singletonList(recipe), pantry).get(0);

        int missingCount = result.getMissing().size();
        if (missingCount == 0) {
            status.setText(R.string.detail_have_everything);
            status.setTextColor(ContextCompat.getColor(this, R.color.fresh_dark));
        } else {
            status.setText(getResources().getQuantityString(
                    R.plurals.detail_missing_count, missingCount, missingCount));
            status.setTextColor(ContextCompat.getColor(this, R.color.urgent_text));
        }

        // Pantry items by matching key, to show "You have 400 g" next to each ingredient
        Map<String, PantryItem> pantryByKey = new HashMap<>();
        for (PantryItem item : pantry) {
            pantryByKey.put(IngredientNormalizer.normalize(item.getName()), item);
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        LinearLayout ingredientList = findViewById(R.id.ingredientList);
        ingredientList.removeAllViews(); // onResume may run more than once
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_detail_ingredient, ingredientList, false);
            // The missing list holds the same ingredient objects as the recipe,
            // so contains() tells us directly whether this one is covered
            boolean covered = !result.getMissing().contains(ingredient);
            PantryItem have = pantryByKey.get(IngredientNormalizer.normalize(ingredient.getName()));
            bindIngredientRow(row, ingredient, have, covered);
            ingredientList.addView(row);
        }

        LinearLayout stepList = findViewById(R.id.stepList);
        stepList.removeAllViews();
        String[] steps = recipe.getSteps().split("\n");
        for (int i = 0; i < steps.length; i++) {
            View row = inflater.inflate(R.layout.item_detail_step, stepList, false);
            ((TextView) row.findViewById(R.id.stepNumber)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.stepText)).setText(steps[i].trim());
            stepList.addView(row);
        }
    }

    /** Fills one ingredient row: ✓/✗, name, amount needed, and what the pantry has. */
    private void bindIngredientRow(View row, RecipeIngredient ingredient,
                                   PantryItem have, boolean covered) {
        TextView mark = row.findViewById(R.id.ingredientMark);
        TextView name = row.findViewById(R.id.ingredientName);
        TextView amount = row.findViewById(R.id.ingredientAmount);
        TextView haveText = row.findViewById(R.id.ingredientHave);

        name.setText(TextFormat.capitalise(ingredient.getName()));
        amount.setText(getString(R.string.quantity_with_unit,
                TextFormat.quantity(ingredient.getQuantity()), ingredient.getUnit()));

        int good = ContextCompat.getColor(this, R.color.fresh_dark);
        int bad = ContextCompat.getColor(this, R.color.urgent_text);

        if (have == null) {
            mark.setText(R.string.mark_missing);
            mark.setTextColor(bad);
            haveText.setText(R.string.detail_not_on_shelf);
            haveText.setTextColor(bad);
            return;
        }

        String haveAmount = getString(R.string.quantity_with_unit,
                TextFormat.quantity(have.getQuantity()), have.getUnit());

        if (covered) {
            mark.setText(R.string.mark_have);
            mark.setTextColor(good);
            haveText.setText(getString(R.string.detail_you_have, haveAmount));
            haveText.setTextColor(ContextCompat.getColor(this, R.color.ink_muted));
            return;
        }

        // On the shelf but not usable: either too little, or a unit that can't be compared
        UnitConverter.Measure haveMeasure = UnitConverter.toBase(have.getQuantity(), have.getUnit());
        UnitConverter.Measure needMeasure = UnitConverter.toBase(ingredient.getQuantity(), ingredient.getUnit());
        boolean comparable = haveMeasure != null && needMeasure != null
                && haveMeasure.dimension == needMeasure.dimension;

        mark.setText(R.string.mark_missing);
        mark.setTextColor(bad);
        haveText.setText(getString(comparable ? R.string.detail_not_enough
                : R.string.detail_different_unit, haveAmount));
        haveText.setTextColor(bad);
    }
}