package com.example.mycontactlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {

    private ListView listViewContacts;
    private TextView textViewNoContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);

        listViewContacts = findViewById(R.id.listViewContacts);
        textViewNoContacts = findViewById(R.id.textViewNoContacts);

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

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        SharedPreferences preferences = getSharedPreferences(
                "MyContactListPreferences",
                Context.MODE_PRIVATE
        );

        String sortField = preferences.getString("sortfield", "contactname");
        String sortOrder = preferences.getString("sortorder", "ASC");

        ContactDataSource dataSource = new ContactDataSource(this);
        ArrayList<String> contacts = new ArrayList<>();

        try {
            dataSource.open();
            contacts.addAll(dataSource.getContactList(sortField, sortOrder));
        } catch (Exception e) {
            Toast.makeText(this, "Could not load contacts.", Toast.LENGTH_LONG)
                    .show();
        } finally {
            dataSource.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                contacts
        );
        listViewContacts.setAdapter(adapter);

        boolean isEmpty = contacts.isEmpty();
        textViewNoContacts.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        listViewContacts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void initListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(view -> loadContacts());
    }

    private void initMapButton() {
        ImageButton ibMap = findViewById(R.id.imageButtonMap);
        ibMap.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ContactListActivity.this,
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
                    ContactListActivity.this,
                    ContactSettingsActivity.class
            );
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}