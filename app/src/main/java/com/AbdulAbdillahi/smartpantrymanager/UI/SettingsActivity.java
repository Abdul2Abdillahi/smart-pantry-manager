package com.AbdulAbdillahi.smartpantrymanager.UI;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.AbdulAbdillahi.smartpantrymanager.R;
import com.AbdulAbdillahi.smartpantrymanager.data.AppSettings;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

/**
 * Lets the user change how the app behaves. Every change is saved the moment
 * it's made, so there's no "Save" button to forget.
 */
public class SettingsActivity extends AppCompatActivity {

    private AppSettings settings;
    private TextView soonDaysLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        settings = new AppSettings(this);
        MaterialSwitch warningsSwitch = findViewById(R.id.warningsSwitch);
        Slider soonDaysSlider = findViewById(R.id.soonDaysSlider);
        MaterialAutoCompleteTextView defaultUnitInput = findViewById(R.id.defaultUnitInput);
        soonDaysLabel = findViewById(R.id.soonDaysLabel);

        // 1. Show the saved values
        warningsSwitch.setChecked(settings.areWarningsEnabled());
        soonDaysSlider.setValue(settings.getSoonDays());
        soonDaysSlider.setEnabled(settings.areWarningsEnabled());
        updateDaysLabel(settings.getSoonDays());
        defaultUnitInput.setSimpleItems(getResources().getStringArray(R.array.units));
        defaultUnitInput.setText(settings.getDefaultUnit(), false);

        // 2. Save each change immediately
        warningsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            settings.setWarningsEnabled(isChecked);
            soonDaysSlider.setEnabled(isChecked); // the number of days only matters when warnings are on
        });
        soonDaysSlider.addOnChangeListener((slider, value, fromUser) -> {
            int days = Math.round(value);
            settings.setSoonDays(days);
            updateDaysLabel(days);
        });
        defaultUnitInput.setOnItemClickListener((parent, view, position, id) ->
                settings.setDefaultUnit((String) parent.getItemAtPosition(position)));
        BottomNav.setup(this, R.id.nav_settings);
    }

    private void updateDaysLabel(int days) {
        soonDaysLabel.setText(getResources().getQuantityString(
                R.plurals.settings_days_label, days, days));
    }
}