package com.example.mycontactlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
}