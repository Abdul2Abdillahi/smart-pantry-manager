package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper;
import com.AbdulAbdillahi.smartpantrymanager.logic.RecipeMatcher;
import com.AbdulAbdillahi.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * BONUS: recipes missing exactly one ingredient. A separate screen, so they are
 * never mixed with the strict "Cook tonight" suggestions (brief section 2.3).
 */
public class AlmostThereActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeCardAdapter adapter;
    private View emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_almost_there);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = DatabaseHelper.getInstance(this);
        emptyText = findViewById(R.id.almostEmpty);

        // Tapping a card opens the detail screen, which shows the missing ingredient with ✗
        adapter = new RecipeCardAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        RecyclerView recycler = findViewById(R.id.almostRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlmostThere(); // the pantry may have changed, so check again
    }

    private void loadAlmostThere() {
        List<RecipeMatcher.MatchResult> results =
                RecipeMatcher.almostThere(db.getAllRecipes(), db.getAllPantryItems());

        List<RecipeCardAdapter.Card> cards = new ArrayList<>();
        for (RecipeMatcher.MatchResult result : results) {
            RecipeIngredient missing = result.getMissing().get(0); // exactly one, by definition
            String amount = getString(R.string.quantity_with_unit,
                    TextFormat.quantity(missing.getQuantity()), missing.getUnit());
            String text = getString(R.string.almost_missing,
                    TextFormat.capitalise(missing.getName()), amount);
            cards.add(new RecipeCardAdapter.Card(result.getRecipe(), text));
        }
        adapter.setCards(cards);
        emptyText.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
    }
}