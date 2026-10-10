package com.AbdulAbdillahi.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.UI.BottomNav;
import com.AbdulAbdillahi.smartpantrymanager.UI.EditIngredientActivity;
import com.AbdulAbdillahi.smartpantrymanager.UI.PantryAdapter;
import com.AbdulAbdillahi.smartpantrymanager.UI.SettingsActivity;
import com.AbdulAbdillahi.smartpantrymanager.UI.ShelfDecoration;
import com.AbdulAbdillahi.smartpantrymanager.UI.SuggestedRecipesActivity;
import com.AbdulAbdillahi.smartpantrymanager.data.AppSettings;
import com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper;
import com.AbdulAbdillahi.smartpantrymanager.logic.Freshness;
import com.AbdulAbdillahi.smartpantrymanager.logic.RecipeMatcher;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;
import com.AbdulAbdillahi.smartpantrymanager.model.Recipe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private static final int JARS_PER_SHELF = 3;

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView subtitle;
    private TextView emptyState;

    // Recipe ids makeable at the last load. Null on the first load, so nothing
    // is announced just for opening the app.
    private Set<Long> makeableIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = DatabaseHelper.getInstance(this);
        subtitle = findViewById(R.id.pantrySubtitle);
        emptyState = findViewById(R.id.pantryEmpty);

        RecyclerView recycler = findViewById(R.id.pantryRecycler);
        recycler.setLayoutManager(new GridLayoutManager(this, JARS_PER_SHELF));
        recycler.addItemDecoration(new ShelfDecoration(this, JARS_PER_SHELF));
        // Tapping a jar opens the same form in "edit" mode, passing the item's id
        adapter = new PantryAdapter(item -> {
            Intent intent = new Intent(this, EditIngredientActivity.class);
            intent.putExtra(EditIngredientActivity.EXTRA_ITEM_ID, item.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        // + button: open the form in "add" mode (no item id passed)
        FloatingActionButton addFab = findViewById(R.id.addFab);
        addFab.setOnClickListener(v ->
                startActivity(new Intent(this, EditIngredientActivity.class)));

        BottomNav.setup(this, R.id.nav_shelf);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible, so changes made on
        // other screens (like Add/Edit) appear as soon as the user returns.
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        int soonDays = new AppSettings(this).effectiveSoonDays();
        adapter.setItems(items, soonDays);

        int needAttention = 0;
        LocalDate today = LocalDate.now();
        for (PantryItem item : items) {
            if (Freshness.of(item.getExpiryDate(), today, soonDays).needsAttention()) {
                needAttention++;
            }
        }

        String text = getResources().getQuantityString(
                R.plurals.pantry_ingredient_count, items.size(), items.size());
        if (needAttention > 0) {
            text += " · " + getString(R.string.pantry_attention, needAttention);
        }
        subtitle.setText(text);

        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        checkForUnlockedRecipes(items);
    }
    /**
     * Compares which recipes can be made now with the last time this screen loaded.
     * Any new ones were unlocked by the change the user just made.
     */
    private void checkForUnlockedRecipes(List<PantryItem> items) {
        List<Recipe> makeable = RecipeMatcher.suggest(db.getAllRecipes(), items);

        Set<Long> nowIds = new HashSet<>();
        List<Recipe> unlocked = new ArrayList<>();
        for (Recipe recipe : makeable) {
            nowIds.add(recipe.getId());
            if (makeableIds != null && !makeableIds.contains(recipe.getId())) {
                unlocked.add(recipe);
            }
        }
        makeableIds = nowIds;

        if (unlocked.isEmpty()) {
            return;
        }

        String message = unlocked.size() == 1
                ? getString(R.string.recipe_unlocked, unlocked.get(0).getName())
                : getResources().getQuantityString(R.plurals.recipes_unlocked,
                unlocked.size(), unlocked.size());

        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.addFab) // sit above the + button, not on top of it
                .setBackgroundTint(ContextCompat.getColor(this, R.color.ink))
                .setTextColor(ContextCompat.getColor(this, android.R.color.white))
                .setActionTextColor(ContextCompat.getColor(this, R.color.soon))
                .setAction(R.string.action_view, v ->
                        BottomNav.open(this, SuggestedRecipesActivity.class))
                .show();
    }
}

