package com.AbdulAbdillahi.smartpantrymanager.UI;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.data.AppSettings;
import com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper;
import com.AbdulAbdillahi.smartpantrymanager.logic.Freshness;
import com.AbdulAbdillahi.smartpantrymanager.logic.IngredientNormalizer;
import com.AbdulAbdillahi.smartpantrymanager.logic.RecipeMatcher;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;
import com.AbdulAbdillahi.smartpantrymanager.model.Recipe;
import com.AbdulAbdillahi.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists ONLY the recipes the user can make right now (strict matching),
 * with recipes that use up soon-to-expire food first.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeCardAdapter adapter;
    private TextView subtitle;
    private View emptyState;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = DatabaseHelper.getInstance(this);
        subtitle = findViewById(R.id.suggestedSubtitle);
        emptyState = findViewById(R.id.emptyState);
        recycler = findViewById(R.id.recipeRecycler);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        findViewById(R.id.goToShelfButton).setOnClickListener(v -> finish());

        // Tapping a card opens the full recipe, passing its id in the Intent
        adapter = new RecipeCardAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        BottomNav.setup(this, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions(); // re-check every time, in case the pantry changed
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> suggested = RecipeMatcher.suggest(db.getAllRecipes(), pantry);

        // Pantry items that should be used soon (expired items are left out on purpose:
        // we never want to encourage cooking with food that has gone off)
        Map<String, PantryItem> useSoonByKey = new HashMap<>();
        LocalDate today = LocalDate.now();
        int soonDays = new AppSettings(this).effectiveSoonDays();
        for (PantryItem item : pantry) {
            Freshness freshness = Freshness.of(item.getExpiryDate(), today, soonDays);
            if (freshness == Freshness.USE_TODAY || freshness == Freshness.USE_SOON) {
                useSoonByKey.put(IngredientNormalizer.normalize(item.getName()), item);
            }
        }

        List<RecipeCardAdapter.Card> cards = new ArrayList<>();
        for (Recipe recipe : suggested) {
            List<String> usesUp = new ArrayList<>();
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                PantryItem item = useSoonByKey.get(IngredientNormalizer.normalize(ingredient.getName()));
                if (item != null) {
                    usesUp.add(item.getName());
                }
            }
            cards.add(new RecipeCardAdapter.Card(recipe, usesUp));
        }

        // Recipes that rescue the most soon-to-expire food come first, then alphabetical
        cards.sort((a, b) -> {
            int byRescue = Integer.compare(b.usesUp.size(), a.usesUp.size());
            return byRescue != 0 ? byRescue : a.recipe.getName().compareToIgnoreCase(b.recipe.getName());
        });
        adapter.setCards(cards);

        boolean empty = cards.isEmpty();
        subtitle.setText(getResources().getQuantityString(
                R.plurals.suggested_subtitle, cards.size(), cards.size()));
        subtitle.setVisibility(empty ? View.GONE : View.VISIBLE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}