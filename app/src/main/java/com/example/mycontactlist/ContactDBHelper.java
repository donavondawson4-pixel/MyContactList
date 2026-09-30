package com.example.mycontactlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mycontacts.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_CONTACT_TABLE =
            "CREATE TABLE contact (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "contactname TEXT NOT NULL, " +
                    "streetaddress TEXT, " +
                    "city TEXT, " +
                    "state TEXT, " +
                    "zipcode TEXT, " +
                    "phonenumber TEXT, " +
                    "cellnumber TEXT, " +
                    "email TEXT, " +
                    "birthday INTEGER)";

    public ContactDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase database,
            int oldVersion,
            int newVersion
    ) {
        database.execSQL("DROP TABLE IF EXISTS contact");
        onCreate(database);
    }
}