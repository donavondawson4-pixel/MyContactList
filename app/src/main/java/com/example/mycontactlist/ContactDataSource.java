package com.example.mycontactlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContactDataSource {
    private static final String TABLE_CONTACT = "contact";

    private final ContactDBHelper dbHelper;
    private SQLiteDatabase database;

    public ContactDataSource(Context context) {
        dbHelper = new ContactDBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insertContact(Contact contact) {
        ContentValues values = makeValues(contact);
        return database.insert(TABLE_CONTACT, null, values) != -1;
    }

    public boolean updateContact(Contact contact) {
        ContentValues values = makeValues(contact);

        return database.update(
                TABLE_CONTACT,
                values,
                "_id = ?",
                new String[]{String.valueOf(contact.getContactId())}
        ) > 0;
    }

    public int getLastContactId() {
        Cursor cursor = database.rawQuery(
                "SELECT MAX(_id) FROM " + TABLE_CONTACT,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return -1;
        } finally {
            cursor.close();
        }
    }

    private ContentValues makeValues(Contact contact) {
        ContentValues values = new ContentValues();
        values.put("contactname", contact.getContactName());
        values.put("streetaddress", contact.getStreetAddress());
        values.put("city", contact.getCity());
        values.put("state", contact.getState());
        values.put("zipcode", contact.getZipCode());
        values.put("phonenumber", contact.getPhoneNumber());
        values.put("cellnumber", contact.getCellNumber());
        values.put("email", contact.getEmail());
        values.put("birthday", contact.getBirthdayMillis());
        return values;
    }
    public List<String> getContactList(String sortField, String sortOrder) {
        String orderColumn;

        if ("city".equalsIgnoreCase(sortField)) {
            orderColumn = "city";
        } else if ("birthday".equalsIgnoreCase(sortField)) {
            orderColumn = "birthday";
        } else {
            orderColumn = "contactname";
        }

        String direction = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";

        String orderBy = orderColumn;
        if (!"birthday".equals(orderColumn)) {
            orderBy += " COLLATE NOCASE";
        }
        orderBy += " " + direction + ", contactname COLLATE NOCASE ASC";

        Cursor cursor = database.query(
                TABLE_CONTACT,
                new String[]{"contactname", "city", "birthday"},
                null,
                null,
                null,
                null,
                orderBy
        );

        List<String> contacts = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String city = cursor.getString(1);
                long birthdayMillis = cursor.getLong(2);

                StringBuilder row = new StringBuilder(name);

                if (city != null && !city.trim().isEmpty()) {
                    row.append("\n").append(city);
                }

                if (birthdayMillis > 0) {
                    String birthday = DateFormat.getDateInstance(DateFormat.MEDIUM)
                            .format(new Date(birthdayMillis));
                    row.append("\nBirthday: ").append(birthday);
                }

                contacts.add(row.toString());
            }
        } finally {
            cursor.close();
        }

        return contacts;
    }
}