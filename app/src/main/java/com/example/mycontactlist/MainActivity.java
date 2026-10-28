package com.example.mycontactlist;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import android.widget.Toast;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Calendar;
import android.content.Intent;


import android.net.Uri;
import android.content.ActivityNotFoundException;
import android.widget.EditText;
import android.view.View;

import android.content.Intent;


import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;



public class MainActivity extends AppCompatActivity
        implements DatePickerDialog.SaveDateListener {
    private Contact currentContact;
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();

                            if (extras != null) {
                                Bitmap photo;
                                if (android.os.Build.VERSION.SDK_INT >= 33) {
                                    photo = extras.getParcelable(
                                            "data",
                                            Bitmap.class
                                    );
                                } else {
                                    photo = extras.getParcelable("data");
                                }


                                if (photo != null) {
                                    ImageView imageContactPhoto =
                                            findViewById(R.id.imageContactPhoto);

                                    imageContactPhoto.setImageBitmap(photo);

                                    java.io.ByteArrayOutputStream stream =
                                            new java.io.ByteArrayOutputStream();

                                    photo.compress(
                                            Bitmap.CompressFormat.JPEG,
                                            80,
                                            stream
                                    );

                                    currentContact.setContactPhoto(
                                            stream.toByteArray()
                                    );
                                }

                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        int contactId = getIntent().getIntExtra("contactID", -1);

        if (contactId == -1) {
            currentContact = new Contact();
        } else {
            ContactDataSource dataSource =
                    new ContactDataSource(this);

            try {
                dataSource.open();
                currentContact =
                        dataSource.getSpecificContact(contactId);
            } catch (Exception e) {
                currentContact = new Contact();

                Toast.makeText(
                        this,
                        "Could not load contact.",
                        Toast.LENGTH_LONG
                ).show();
            } finally {
                dataSource.close();
            }
        }

        initTextChangedEvents();
        initSaveButton();
        displayContact();


        initListButton();
        initMapButton();
        initSettingsButton();
        initToggleButton();
        setForEditing(false);
        initChangeDateButton();
        initCallFunction();
        initCameraButton();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void didFinishDatePickerDialog(Calendar selectedTime) {
        TextView birthday =
                findViewById(R.id.textBirthday);

        birthday.setText(
                DateFormat.format("MM/dd/yyyy", selectedTime)
        );
        currentContact.setBirthdayMillis(selectedTime.getTimeInMillis());
    }
    private void initCameraButton() {
        Button takePhoto = findViewById(R.id.buttonTakePhoto);
        takePhoto.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
            );
            try {
                cameraLauncher.launch(cameraIntent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(
                        this,
                        "Camera not available",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    private void initChangeDateButton() {
        Button changeDate =
                findViewById(R.id.btnBirthday);

        changeDate.setOnClickListener(
                view -> {
                    FragmentManager fm =
                            getSupportFragmentManager();

                    DatePickerDialog dialog =
                            new DatePickerDialog();

                    dialog.show(fm, "DatePick");
                });
    }
    private void initCallFunction() {
        EditText editHome = findViewById(R.id.editHome);
        EditText editCell = findViewById(R.id.editCell);

        editHome.setLongClickable(true);
        editCell.setLongClickable(true);

        editHome.setTextIsSelectable(false);
        editCell.setTextIsSelectable(false);


        editHome.setOnLongClickListener(view -> {
            Toast.makeText(
                    MainActivity.this,
                    "Opening dialer",
                    Toast.LENGTH_SHORT
            ).show();

            openDialer(editHome.getText().toString());
            return true;
        });

        editCell.setOnLongClickListener(view -> {
            openDialer(editCell.getText().toString());
            return true;
        });
    }


    private void openDialer(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "No phone number available",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent = new Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", phoneNumber.trim(), null)
        );

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "No dialer app installed",
                    Toast.LENGTH_LONG
            ).show();
        }
    }



    private void initToggleButton() {
        final ToggleButton editToggle =
                findViewById(R.id.toggleButtonEdit);

        editToggle.setOnClickListener(view -> setForEditing(editToggle.isChecked()));
    }
    private void setForEditing(boolean enabled) {
        EditText editName = findViewById(R.id.editName);
        EditText editAddress = findViewById(R.id.editAddress);
        EditText editCity = findViewById(R.id.editCity);
        EditText editState = findViewById(R.id.editState);
        EditText editZipcode = findViewById(R.id.editZipcode);
        EditText editHome = findViewById(R.id.editHome);
        EditText editCell = findViewById(R.id.editCell);
        EditText editEMail = findViewById(R.id.editEMail);

        Button buttonBirthday = findViewById(R.id.btnBirthday);
        Button buttonSave = findViewById(R.id.ButtonSave);

        editName.setEnabled(enabled);
        editAddress.setEnabled(enabled);
        editCity.setEnabled(enabled);
        editState.setEnabled(enabled);
        editZipcode.setEnabled(enabled);
        editHome.setEnabled(true);
        editCell.setEnabled(true);

        editHome.setShowSoftInputOnFocus(enabled);
        editCell.setShowSoftInputOnFocus(enabled);

        editEMail.setEnabled(enabled);

        buttonBirthday.setEnabled(enabled);
        buttonSave.setEnabled(enabled);

        if (enabled) {
            editHome.setInputType(
                    android.text.InputType.TYPE_CLASS_PHONE
            );
            editCell.setInputType(
                    android.text.InputType.TYPE_CLASS_PHONE
            );
            editName.requestFocus();
        } else {
            editHome.setInputType(
                    android.text.InputType.TYPE_NULL
            );
            editCell.setInputType(
                    android.text.InputType.TYPE_NULL
            );
        }
    }
    private void initListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);

        ibList.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
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
                    MainActivity.this,
                    ContactMapActivity.class
            );

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void initSettingsButton() {
        ImageButton ibSettings = findViewById(R.id.imageButtonSettings);

        ibSettings.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ContactSettingsActivity.class
            );

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

    }
    private void initTextChangedEvents() {
        watchField(R.id.editName, currentContact::setContactName);
        watchField(R.id.editAddress, currentContact::setStreetAddress);
        watchField(R.id.editCity, currentContact::setCity);
        watchField(R.id.editState, currentContact::setState);
        watchField(R.id.editZipcode, currentContact::setZipCode);
        watchField(R.id.editHome, currentContact::setPhoneNumber);
        watchField(R.id.editCell, currentContact::setCellNumber);
        watchField(R.id.editEMail, currentContact::setEmail);
    }

    private void watchField(int fieldId, ContactTextSetter setter) {
        EditText field = findViewById(fieldId);

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence text, int start, int count, int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text, int start, int before, int count
            ) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                setter.set(text.toString());
            }
        });
    }

    private interface ContactTextSetter {
        void set(String value);
    }
    private void initSaveButton() {
        Button saveButton = findViewById(R.id.ButtonSave);

        saveButton.setOnClickListener(view -> {
            if (currentContact.getContactName().trim().isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "Enter a contact name first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            ContactDataSource dataSource =
                    new ContactDataSource(MainActivity.this);
            dataSource.open();

            boolean wasSuccessful;

            if (currentContact.getContactId() == -1) {
                wasSuccessful = dataSource.insertContact(currentContact);

                if (wasSuccessful) {
                    currentContact.setContactId(dataSource.getLastContactId());
                }
            } else {
                wasSuccessful = dataSource.updateContact(currentContact);
            }

            dataSource.close();

            if (wasSuccessful) {
                ToggleButton editToggle =
                        findViewById(R.id.toggleButtonEdit);
                editToggle.setChecked(false);
                setForEditing(false);

                Toast.makeText(
                        MainActivity.this,
                        "Contact saved",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        MainActivity.this,
                        "Unable to save contact",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    private void displayContact() {

        EditText editName = findViewById(R.id.editName);
        EditText editAddress = findViewById(R.id.editAddress);
        EditText editCity = findViewById(R.id.editCity);
        EditText editState = findViewById(R.id.editState);
        EditText editZipcode = findViewById(R.id.editZipcode);
        EditText editHome = findViewById(R.id.editHome);
        EditText editCell = findViewById(R.id.editCell);
        EditText editEMail = findViewById(R.id.editEMail);
        TextView birthday = findViewById(R.id.textBirthday);

        editName.setText(currentContact.getContactName());
        editAddress.setText(currentContact.getStreetAddress());
        editCity.setText(currentContact.getCity());
        editState.setText(currentContact.getState());
        editZipcode.setText(currentContact.getZipCode());
        editHome.setText(currentContact.getPhoneNumber());
        editCell.setText(currentContact.getCellNumber());
        editEMail.setText(currentContact.getEmail());

        ImageView imageContactPhoto =
                findViewById(R.id.imageContactPhoto);

        byte[] photoData = currentContact.getContactPhoto();

        if (photoData != null && photoData.length > 0) {
            Bitmap photo = android.graphics.BitmapFactory.decodeByteArray(
                    photoData,
                    0,
                    photoData.length
            );
            imageContactPhoto.setImageBitmap(photo);
        } else {
            imageContactPhoto.setImageDrawable(null);
        }

        if (currentContact.getBirthdayMillis() > 0) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(
                    currentContact.getBirthdayMillis()
            );

            birthday.setText(
                    DateFormat.format(
                            "MM/dd/yyyy",
                            calendar
                    )
            );
        } else {
            birthday.setText("");
        }
    }
}