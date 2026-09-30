package com.example.mycontactlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContactSettingsActivity extends AppCompatActivity {

    private static final String PREFERENCES_NAME =
            "MyContactListPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_settings);

        ScrollView scrollViewObject = findViewById(R.id.scrollViewSettings);

        SharedPreferences preferences = getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );

        String backgroundColor =
                preferences.getString("backgroundcolor", "white");

        if ("blue".equals(backgroundColor)) {
            scrollViewObject.setBackgroundResource(
                    R.color.settings_background_blue
            );
        } else if ("green".equals(backgroundColor)) {
            scrollViewObject.setBackgroundResource(
                    R.color.settings_background_green
            );
        } else {
            backgroundColor = "white";
            scrollViewObject.setBackgroundResource(R.color.white);
        }

        initSettings();
        initBackgroundColor(scrollViewObject, backgroundColor);
        initListButton();
        initMapButton();
        initSettingsButton();

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );
    }

    private void initSettings() {
        SharedPreferences preferences = getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );

        String sortBy = preferences.getString("sortfield", "contactname");
        String sortOrder = preferences.getString("sortorder", "ASC");

        RadioButton rbName = findViewById(R.id.radioName);
        RadioButton rbCity = findViewById(R.id.radioCity);
        RadioButton rbBirthday = findViewById(R.id.radioBirthday);
        RadioButton rbAscending = findViewById(R.id.radioAscending);
        RadioButton rbDescending = findViewById(R.id.radioDescending);

        if (sortBy.equalsIgnoreCase("contactname")) {
            rbName.setChecked(true);
        } else if (sortBy.equalsIgnoreCase("city")) {
            rbCity.setChecked(true);
        } else {
            rbBirthday.setChecked(true);
        }

        if (sortOrder.equalsIgnoreCase("ASC")) {
            rbAscending.setChecked(true);
        } else {
            rbDescending.setChecked(true);
        }

        RadioGroup sortByGroup = findViewById(R.id.radioGroupSortBy);
        sortByGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedSortBy;

            if (checkedId == R.id.radioName) {
                selectedSortBy = "contactname";
            } else if (checkedId == R.id.radioCity) {
                selectedSortBy = "city";
            } else {
                selectedSortBy = "birthday";
            }

            preferences.edit()
                    .putString("sortfield", selectedSortBy)
                    .apply();
        });

        RadioGroup sortOrderGroup = findViewById(R.id.radioGroupSortOrder);
        sortOrderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedSortOrder =
                    checkedId == R.id.radioAscending ? "ASC" : "DESC";

            preferences.edit()
                    .putString("sortorder", selectedSortOrder)
                    .apply();
        });
    }

    private void initBackgroundColor(
            ScrollView scrollViewObject,
            String selectedColor
    ) {
        RadioButton rbWhite = findViewById(R.id.radioBackgroundWhite);
        RadioButton rbBlue = findViewById(R.id.radioBackgroundBlue);
        RadioButton rbGreen = findViewById(R.id.radioBackgroundGreen);

        if ("blue".equals(selectedColor)) {
            rbBlue.setChecked(true);
        } else if ("green".equals(selectedColor)) {
            rbGreen.setChecked(true);
        } else {
            rbWhite.setChecked(true);
        }

        RadioGroup backgroundColorGroup =
                findViewById(R.id.radioGroupBackgroundColor);

        backgroundColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newColor;

            if (checkedId == R.id.radioBackgroundBlue) {
                newColor = "blue";
                scrollViewObject.setBackgroundResource(
                        R.color.settings_background_blue
                );
            } else if (checkedId == R.id.radioBackgroundGreen) {
                newColor = "green";
                scrollViewObject.setBackgroundResource(
                        R.color.settings_background_green
                );
            } else {
                newColor = "white";
                scrollViewObject.setBackgroundResource(R.color.white);
            }

            getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString("backgroundcolor", newColor)
                    .apply();
        });
    }

    private void initListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);

        ibList.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ContactSettingsActivity.this,
                    ContactListActivity.class
            );
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initMapButton() {
        ImageButton ibMap = findViewById(R.id.imageButtonMap);

        ibMap.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ContactSettingsActivity.this,
                    ContactActivity.class
            );
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initSettingsButton() {
        ImageButton ibSettings = findViewById(R.id.imageButtonSettings);

        ibSettings.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ContactSettingsActivity.this,
                    ContactSettingsActivity.class
            );
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}