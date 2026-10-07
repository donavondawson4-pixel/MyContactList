package com.example.mycontactlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import java.util.ArrayList;
import android.widget.Switch;

public class ContactListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewContacts;

    private ArrayList<Contact> contacts;
    private TextView textViewNoContacts;

    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);

        recyclerViewContacts = findViewById(R.id.rvContacts);
        textViewNoContacts = findViewById(R.id.textViewNoContacts);

        initListButton();
        initMapButton();
        initSettingsButton();
        initAddContactButton();
        initDeleteSwitch();

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
    private void initAddContactButton() {
        Button newContact = findViewById(R.id.buttonAddContact);

        newContact.setOnClickListener(view -> {
            Intent intent = new Intent(
                    ContactListActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
        });
    }

    private void loadContacts() {

        ContactDataSource dataSource = new ContactDataSource(this);

        try {
            dataSource.open();
            contacts = dataSource.getContacts();
        } catch (Exception e) {
            contacts = new ArrayList<>();

            Toast.makeText(
                    this,
                    "Could not load contacts.",
                    Toast.LENGTH_LONG
            ).show();
        } finally {
            dataSource.close();
        }

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this);

        recyclerViewContacts.setLayoutManager(layoutManager);

        contactAdapter =
                new ContactAdapter(contacts);

        Switch deleteSwitch =
                findViewById(R.id.switchDelete);

        contactAdapter.setDelete(
                deleteSwitch.isChecked()
        );

        contactAdapter.setOnItemClickListener(view -> {

            RecyclerView.ViewHolder viewHolder =
                    (RecyclerView.ViewHolder) view.getTag();

            int position =
                    viewHolder.getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {

                int contactId =
                        contacts.get(position).getContactId();

                Intent intent = new Intent(
                        ContactListActivity.this,
                        MainActivity.class
                );

                intent.putExtra(
                        "contactID",
                        contactId
                );

                startActivity(intent);
            }
        });
        contactAdapter.setOnDeleteClickListener(view -> {

            RecyclerView.ViewHolder viewHolder =
                    (RecyclerView.ViewHolder) view.getTag();

            int position =
                    viewHolder.getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {

                int contactId =
                        contacts.get(position).getContactId();

                ContactDataSource deleteDataSource =
                        new ContactDataSource(
                                ContactListActivity.this
                        );

                boolean wasDeleted = false;

                try {
                    deleteDataSource.open();

                    wasDeleted =
                            deleteDataSource.deleteContact(contactId);

                } catch (Exception e) {

                    Toast.makeText(
                            ContactListActivity.this,
                            "Could not delete contact.",
                            Toast.LENGTH_LONG
                    ).show();

                } finally {
                    deleteDataSource.close();
                }

                if (wasDeleted) {

                    Toast.makeText(
                            ContactListActivity.this,
                            "Contact deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadContacts();
                }
            }
        });

        recyclerViewContacts.setAdapter(
                contactAdapter
        );

        boolean isEmpty =
                contacts.isEmpty();

        textViewNoContacts.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        recyclerViewContacts.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE
        );
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
    private void initDeleteSwitch() {

        Switch deleteSwitch =
                findViewById(R.id.switchDelete);

        deleteSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (contactAdapter != null) {
                        contactAdapter.setDelete(isChecked);
                    }
                }
        );
    }
}