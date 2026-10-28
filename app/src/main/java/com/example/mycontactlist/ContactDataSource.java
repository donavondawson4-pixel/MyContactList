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
        values.put("contactphoto", contact.getContactPhoto());
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
    public ArrayList<String> getContactName() {
        ArrayList<String> contactNames = new ArrayList<>();

        try {
            String query = "Select contactname from contact";
            Cursor cursor = database.rawQuery(query, null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                contactNames.add(cursor.getString(0));
                cursor.moveToNext();
            }

            cursor.close();
        }
        catch (Exception e) {
            contactNames = new ArrayList<>();
        }

        return contactNames;
    }
    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + TABLE_CONTACT,
                null
        );

        try {
            while (cursor.moveToNext()) {
                Contact newContact = new Contact();

                newContact.setContactId(cursor.getInt(0));
                newContact.setContactName(cursor.getString(1));
                newContact.setStreetAddress(cursor.getString(2));
                newContact.setCity(cursor.getString(3));
                newContact.setState(cursor.getString(4));
                newContact.setZipCode(cursor.getString(5));
                newContact.setPhoneNumber(cursor.getString(6));
                newContact.setCellNumber(cursor.getString(7));
                newContact.setEmail(cursor.getString(8));
                newContact.setBirthdayMillis(cursor.getLong(9));

                contacts.add(newContact);
            }
        } finally {
            cursor.close();
        }

        return contacts;
    }
    public Contact getSpecificContact(int contactId) {
        Contact contact = new Contact();

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + TABLE_CONTACT + " WHERE _id = ?",
                new String[]{String.valueOf(contactId)}
        );

        try {
            if (cursor.moveToFirst()) {
                contact.setContactId(cursor.getInt(0));
                contact.setContactName(cursor.getString(1));
                contact.setStreetAddress(cursor.getString(2));
                contact.setCity(cursor.getString(3));
                contact.setState(cursor.getString(4));
                contact.setZipCode(cursor.getString(5));
                contact.setPhoneNumber(cursor.getString(6));
                contact.setCellNumber(cursor.getString(7));
                contact.setEmail(cursor.getString(8));
                contact.setBirthdayMillis(cursor.getLong(9));
                int photoIndex = cursor.getColumnIndex("contactphoto");

                if (photoIndex >=0 && !cursor.isNull(photoIndex)) {
                    contact.setContactPhoto(cursor.getBlob(photoIndex));
                }
            }
        } finally {
            cursor.close();
        }

        return contact;
    }
    public boolean deleteContact(int contactId) {
        boolean didDelete = false;

        try {
            didDelete = database.delete(
                    TABLE_CONTACT,
                    "_id = ?",
                    new String[]{String.valueOf(contactId)}
            ) > 0;
        } catch (Exception e) {
            // Return value remains false if delete fails
        }

        return didDelete;
    }
}