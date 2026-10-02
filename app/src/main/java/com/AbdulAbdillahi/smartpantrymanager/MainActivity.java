package com.AbdulAbdillahi.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.AbdulAbdillahi.smartpantrymanager.UI.PantryAdapter;
import com.AbdulAbdillahi.smartpantrymanager.UI.ShelfDecoration;
import com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper;
import com.AbdulAbdillahi.smartpantrymanager.logic.Freshness;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;

import java.time.LocalDate;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int JARS_PER_SHELF = 3;

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView subtitle;
    private TextView emptyState;

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
        adapter = new PantryAdapter(item ->
                // Editing comes next session; for now, confirm taps work
                Toast.makeText(this, item.getName(), Toast.LENGTH_SHORT).show());
        recycler.setAdapter(adapter);
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
        adapter.setItems(items);

        int needAttention = 0;
        LocalDate today = LocalDate.now();
        for (PantryItem item : items) {
            if (Freshness.of(item.getExpiryDate(), today, Freshness.DEFAULT_SOON_DAYS).needsAttention()) {
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
    }
}

