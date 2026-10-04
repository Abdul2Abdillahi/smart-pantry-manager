package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.data.DatabaseHelper;
import com.AbdulAbdillahi.smartpantrymanager.model.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;

/**
 * One screen for both adding and editing a pantry item.
 * If the Intent carries an item id we're editing; otherwise we're adding.
 */
public class EditIngredientActivity extends AppCompatActivity {

    /** Intent extra: id of the item to edit. Absent when adding a new item. */
    public static final String EXTRA_ITEM_ID = "com.AbdulAbdillahi.smartpantrymanager.EXTRA_ITEM_ID";

    private static final String STATE_EXPIRY = "state_expiry";
    private static final double MAX_QUANTITY = 100_000;

    private DatabaseHelper db;
    private PantryItem editingItem;   // null when adding
    private String selectedExpiry;    // "yyyy-MM-dd", or null for no expiry

    private TextInputLayout nameLayout, quantityLayout, unitLayout, expiryLayout;
    private TextInputEditText nameInput, quantityInput, expiryInput;
    private MaterialAutoCompleteTextView unitInput;
    private MaterialButton removeExpiryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_ingredient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = DatabaseHelper.getInstance(this);
        bindViews();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        unitInput.setSimpleItems(getResources().getStringArray(R.array.units));
        expiryInput.setOnClickListener(v -> showDatePicker());
        removeExpiryButton.setOnClickListener(v -> setExpiry(null));

        // Which mode? Read the item id the shelf screen passed in the Intent.
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = db.getPantryItem(itemId);
        }

        MaterialButton saveButton = findViewById(R.id.saveButton);
        MaterialButton deleteButton = findViewById(R.id.deleteButton);

        if (editingItem != null) {
            toolbar.setTitle(R.string.title_edit_ingredient);
            saveButton.setText(R.string.action_save_changes);
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle(R.string.title_add_ingredient);
        }

        if (savedInstanceState != null) {
            // Screen was recreated (e.g. rotated): the text fields restore themselves,
            // we only need to restore our own expiry value.
            setExpiry(savedInstanceState.getString(STATE_EXPIRY));
        } else if (editingItem != null) {
            fillForm(editingItem);
        } else {
            setExpiry(null);
        }

        saveButton.setOnClickListener(v -> save());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    /** Asks before deleting: a deleted item can't be recovered. */
    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.delete_title, editingItem.getName()))
                .setMessage(R.string.delete_message)
                .setNegativeButton(R.string.delete_cancel, null)
                .setPositiveButton(R.string.delete_confirm, (dialog, which) -> {
                    db.deletePantryItem(editingItem.getId());
                    Toast.makeText(this,
                            getString(R.string.toast_deleted, editingItem.getName()),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Without this, rotating the phone would lose the chosen expiry date
        outState.putString(STATE_EXPIRY, selectedExpiry);
    }

    private void bindViews() {
        nameLayout = findViewById(R.id.nameLayout);
        quantityLayout = findViewById(R.id.quantityLayout);
        unitLayout = findViewById(R.id.unitLayout);
        expiryLayout = findViewById(R.id.expiryLayout);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitInput = findViewById(R.id.unitInput);
        expiryInput = findViewById(R.id.expiryInput);
        removeExpiryButton = findViewById(R.id.removeExpiryButton);
    }

    /** Pre-fills the form with an existing item's values (edit mode). */
    private void fillForm(PantryItem item) {
        nameInput.setText(item.getName());
        quantityInput.setText(PantryAdapter.formatQuantity(item.getQuantity()));
        unitInput.setText(item.getUnit(), false); // false = don't filter the dropdown
        setExpiry(item.getExpiryDate());
    }

    private void showDatePicker() {
        LocalDate start = selectedExpiry != null ? LocalDate.parse(selectedExpiry) : LocalDate.now();
        // DatePickerDialog counts months from 0 (January = 0); LocalDate counts from 1
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> setExpiry(LocalDate.of(year, month + 1, day).toString()),
                start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth());
        if (editingItem == null) {
            // A brand-new item can't already be expired, so block past dates
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }
        dialog.show();
    }

    /** Stores the expiry date and shows it in a friendly format like "4 Oct 2026". */
    private void setExpiry(String isoDate) {
        selectedExpiry = isoDate;
        if (isoDate == null) {
            expiryInput.setText("");
            removeExpiryButton.setVisibility(View.GONE);
        } else {
            LocalDate date = LocalDate.parse(isoDate);
            expiryInput.setText(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            removeExpiryButton.setVisibility(View.VISIBLE);
        }
        expiryLayout.setError(null);
    }

    private void save() {
        if (!validateForm()) {
            return;
        }

        String name = textOf(nameInput);
        double quantity = Double.parseDouble(textOf(quantityInput).replace(',', '.'));
        String unit = textOf(unitInput);

        boolean success;
        if (editingItem == null) {
            PantryItem item = new PantryItem(name, quantity, unit, selectedExpiry);
            success = db.insertPantryItem(item) != -1;
        } else {
            if (!unit.equals(editingItem.getUnit())) {
                // Unit changed, so the old "full jar" amount no longer makes sense
                editingItem.setInitialQuantity(quantity);
            }
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(selectedExpiry);
            success = db.updatePantryItem(editingItem);
        }

        if (!success) {
            // The database's UNIQUE rule rejected it: this ingredient already exists
            nameLayout.setError(getString(R.string.error_duplicate));
            nameInput.requestFocus();
            return;
        }

        Toast.makeText(this, getString(R.string.toast_saved, name), Toast.LENGTH_SHORT).show();
        finish(); // back to the shelf, whose onResume() reloads the list
    }

    /**
     * Checks every field and shows an error under each invalid one.
     * Returns true only if everything is valid.
     */
    private boolean validateForm() {
        boolean valid = true;

        // Name: required
        if (textOf(nameInput).isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        } else {
            nameLayout.setError(null);
        }

        // Quantity: required, a number, above 0, not absurdly large.
        // Commas are accepted because South African keyboards often use "0,5".
        String quantityText = textOf(quantityInput).replace(',', '.');
        if (quantityText.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else {
            try {
                double quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    quantityLayout.setError(getString(R.string.error_quantity_invalid));
                    valid = false;
                } else if (quantity > MAX_QUANTITY) {
                    quantityLayout.setError(getString(R.string.error_quantity_too_large));
                    valid = false;
                } else {
                    quantityLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                quantityLayout.setError(getString(R.string.error_quantity_invalid));
                valid = false;
            }
        }

        // Unit: must be one from the list
        String[] units = getResources().getStringArray(R.array.units);
        if (!Arrays.asList(units).contains(textOf(unitInput))) {
            unitLayout.setError(getString(R.string.error_unit_required));
            valid = false;
        } else {
            unitLayout.setError(null);
        }

        // Expiry: optional, but a new item can't already be expired (safety net behind the picker)
        if (editingItem == null && selectedExpiry != null
                && LocalDate.parse(selectedExpiry).isBefore(LocalDate.now())) {
            expiryLayout.setError(getString(R.string.error_expiry_past));
            valid = false;
        } else {
            expiryLayout.setError(null);
        }

        return valid;
    }

    /** Trimmed text of a field, never null. */
    private static String textOf(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }
}