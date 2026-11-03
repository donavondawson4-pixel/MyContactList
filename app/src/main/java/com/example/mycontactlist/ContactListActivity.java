
package com.example.mycontactlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.ads.MobileAds;


import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;


public class ContactListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewContacts;
    private ArrayList<Contact> contacts;
    private TextView textViewNoContacts;
    private TextView textBatteryLevel;
    private ContactAdapter contactAdapter;


    private boolean batteryReceiverRegistered = false;

    private final BroadcastReceiver batteryReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    int batteryLevel = intent.getIntExtra(
                            BatteryManager.EXTRA_LEVEL, -1
                    );

                    int levelScale = intent.getIntExtra(
                            BatteryManager.EXTRA_SCALE, -1
                    );

                    if (batteryLevel >= 0 && levelScale > 0) {

                        int batteryPercent = Math.round(
                                batteryLevel * 100f / levelScale
                        );

                        textBatteryLevel.setText(
                                batteryPercent + "%"
                        );
                    } else {
                        textBatteryLevel.setText("--%");
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_list);

        AdView adView = findViewById(R.id.adView);

        AdRequest adRequest =
                new AdRequest.Builder().build();

        adView.loadAd(adRequest);


        new Thread(() -> {
            MobileAds.initialize(this, initializationStatus -> {
                Log.d("MyContactList", "AdMob initialized");
            });
        }).start();


        recyclerViewContacts = findViewById(R.id.rvContacts);
        textViewNoContacts = findViewById(R.id.textViewNoContacts);
        textBatteryLevel = findViewById(R.id.textBatteryLevel);

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
    protected void onStart() {
        super.onStart();


        IntentFilter filter = new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
        );

        if (!batteryReceiverRegistered) {
            registerReceiver(batteryReceiver, filter);
            batteryReceiverRegistered = true;
        }
    }

    @Override
    protected void onStop() {


        if (batteryReceiverRegistered) {
            unregisterReceiver(batteryReceiver);
            batteryReceiverRegistered = false;
        }

        super.onStop();
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

        Log.d("MyContactList", "Loading contacts from REST API");

        RestClient.executeGetRequest(
                CONTACTSAPI,
                this,
                new VolleyCallback() {

                    @Override
                    public void onSuccess(ArrayList<Contact> result) {

                        contacts = result;

                        Log.d("MyContactList",
                                "Contacts loaded: " + contacts.size());

                        RecyclerView.LayoutManager layoutManager =
                                new LinearLayoutManager(
                                        ContactListActivity.this);

                        recyclerViewContacts.setLayoutManager(layoutManager);

                        contactAdapter = new ContactAdapter(contacts);

                        Switch deleteSwitch =
                                findViewById(R.id.switchDelete);

                        contactAdapter.setDelete(
                                deleteSwitch.isChecked()
                        );


                        contactAdapter.setOnItemClickListener(view -> {

                            RecyclerView.ViewHolder viewHolder =
                                    (RecyclerView.ViewHolder) view.getTag();

                            int position = viewHolder.getAdapterPosition();

                            if (position != RecyclerView.NO_POSITION) {

                                int contactId =
                                        contacts.get(position).getContactId();

                                Intent intent = new Intent(
                                        ContactListActivity.this,
                                        MainActivity.class
                                );

                                intent.putExtra("contactID", contactId);

                                startActivity(intent);
                            }
                        });


                        contactAdapter.setOnDeleteClickListener(view -> {

                            RecyclerView.ViewHolder viewHolder =
                                    recyclerViewContacts.findContainingViewHolder(view);

                            if (viewHolder == null) {
                                return;
                            }

                            int position = viewHolder.getBindingAdapterPosition();

                            if (position == RecyclerView.NO_POSITION) {
                                return;
                            }

                            Contact contactToDelete = contacts.get(position);

                            int contactId = contactToDelete.getContactId();
                            if (contactId <= 0) {
                                Toast.makeText(
                                        ContactListActivity.this,
                                        "Invalid contact ID",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }
                            new AlertDialog.Builder(ContactListActivity.this)
                                    .setTitle("Delete Contact")
                                    .setMessage(
                                            "Are you sure you want to delete "
                                                    + contactToDelete.getContactName()
                                                    + "?"
                                    )
                                    .setNegativeButton("Cancel", (dialog, which) -> {
                                        dialog.dismiss();
                                    })
                                    .setPositiveButton("Delete", (dialog, which) -> {

                                        Log.d("MyContactList",
                                                "Deleting contact ID: " + contactId);

                                        RestClient.executeDeleteRequest(
                                                contactToDelete,
                                                CONTACTSAPI + contactId,
                                                ContactListActivity.this,
                                                new VolleyCallback() {

                                                    @Override
                                                    public void onSuccess(
                                                            ArrayList<Contact> result) {

                                                        Toast.makeText(
                                                                ContactListActivity.this,
                                                                "Contact deleted successfully",
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                        // Refresh contacts from the API
                                                        loadContacts();
                                                    }
                                                }
                                        );
                                    })
                                    .show();
                        });

                        recyclerViewContacts.setAdapter(contactAdapter);

                        boolean isEmpty = contacts.isEmpty();

                        textViewNoContacts.setVisibility(
                                isEmpty ? View.VISIBLE : View.GONE
                        );

                        recyclerViewContacts.setVisibility(
                                isEmpty ? View.GONE : View.VISIBLE
                        );
                    }
                }
        );
    }


    private void initListButton() {

        ImageButton ibList =
                findViewById(R.id.imageButtonList);

        ibList.setOnClickListener(
                view -> loadContacts()
        );
    }

    private void initMapButton() {

        ImageButton ibMap =
                findViewById(R.id.imageButtonMap);

        ibMap.setOnClickListener(view -> {

            Intent intent = new Intent(
                    ContactListActivity.this,
                    ContactActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);
        });
    }

    private void initSettingsButton() {

        ImageButton ibSettings =
                findViewById(R.id.imageButtonSettings);

        ibSettings.setOnClickListener(view -> {

            Intent intent = new Intent(
                    ContactListActivity.this,
                    ContactSettingsActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

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

    public static final String CONTACTSAPI =
            "https://fvtcdp.azurewebsites.net/api/Contact/";

}
