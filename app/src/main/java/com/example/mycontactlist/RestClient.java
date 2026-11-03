package com.example.mycontactlist;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
public class RestClient {

    private static final String TAG = "MyContactList";

    public static void executeGetRequest(
            String url,
            Context context,
            VolleyCallback volleyCallback) {
        Log.d(TAG, "GET request:" + url);

        RequestQueue queue =
                Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        ArrayList<Contact> contacts =
                                new ArrayList<>();
                        JSONArray items =
                                new JSONArray(response);

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject object =
                                    items.getJSONObject(i);

                            Contact contact = new Contact();

                            contact.setContactId(object.getInt("id"));
                            contact.setContactName(object.optString("contactName", ""));
                            contact.setStreetAddress(object.optString("streetAddress", ""));
                            contact.setCity(object.optString("city", ""));
                            contact.setState(object.optString("state", ""));
                            contact.setZipCode(object.optString("zipCode", ""));
                            contact.setPhoneNumber(object.optString("phoneNumber", ""));
                            contact.setCellNumber(object.optString("cellNumber", ""));
                            contact.setEmail(object.optString("email", ""));

                            if (!object.isNull("birthday")) {
                                contact.setBirthdayMillis(
                                        object.getLong("birthday"));
                            }

                            if (!object.isNull("photo")) {
                                String photo = object.optString("photo", "");

                                if (!photo.isEmpty()
                                        && !photo.equalsIgnoreCase("null")) {
                                    try {
                                        byte[] bytes = Base64.decode(
                                                photo, Base64.DEFAULT);

                                        Bitmap bitmap =
                                                BitmapFactory.decodeByteArray(
                                                        bytes, 0, bytes.length);

                                        contact.setPicture(bitmap);
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "Invalid contact photo", e);
                                    }
                                }
                            }

                            contacts.add(contact);
                        }
                        Log.d(TAG, "Contacts loaded: " + contacts.size());

                        volleyCallback.onSuccess(contacts);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing contacts", e);
                    }
                },
                error -> {
                    Log.e(TAG, "GET request failed", error);
                    Toast.makeText(context,
                            "Unable to retrieve contacts",
                            Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }

    public static void executeGetOneRequest(
            String url,
            Context context,
            VolleyCallback volleyCallback) {

        Log.d(TAG, "Getting one contact: " + url);

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);

                        Contact contact = new Contact();
                        contact.setContactId(object.getInt("id"));
                        contact.setContactName(
                                object.optString("contactName", ""));
                        contact.setStreetAddress(
                                object.optString("streetAddress", ""));
                        contact.setCity(
                                object.optString("city", ""));
                        contact.setState(
                                object.optString("state", ""));
                        contact.setZipCode(
                                object.optString("zipCode", ""));
                        contact.setPhoneNumber(
                                object.optString("phoneNumber", ""));
                        contact.setCellNumber(
                                object.optString("cellNumber", ""));
                        contact.setEmail(
                                object.optString("email", ""));

                        if (!object.isNull("birthday")) {
                            String birthday =
                                    object.optString("birthday", "0");

                            try {
                                contact.setBirthdayMillis(
                                        Long.parseLong(birthday));
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Invalid birthday", e);
                            }
                        }
                        if (!object.isNull("photo")) {
                            String photo =
                                    object.optString("photo", "");

                            if (!photo.isEmpty()
                                    && !photo.equalsIgnoreCase("null")) {
                                try {
                                    byte[] bytes = Base64.decode(
                                            photo, Base64.DEFAULT);

                                    Bitmap bitmap =
                                            BitmapFactory.decodeByteArray(
                                                    bytes, 0, bytes.length);

                                    contact.setPicture(bitmap);

                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, "Invalid photo", e);
                                }
                            }
                        }
                        ArrayList<Contact> contacts =
                                new ArrayList<>();

                        contacts.add(contact);

                        Log.d(TAG, "Contact retrieved: "
                                + contact.getContactName());

                        volleyCallback.onSuccess(contacts);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing contact", e);
                    }
                },
                error -> {
                    Log.e(TAG, "GET one contact failed", error);

                    Toast.makeText(
                            context,
                            "Unable to retrieve contact",
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        queue.add(request);
    }

    public static void executePostRequest(
            Contact contact,
            String url,
            Context context,
            VolleyCallback volleyCallback) {

        executeRequest(
                contact,
                url,
                context,
                volleyCallback,
                Request.Method.POST
        );
    }

    public static void executePutRequest(
            Contact contact,
            String url,
            Context context,
            VolleyCallback volleyCallback) {

        executeRequest(
                contact,
                url,
                context,
                volleyCallback,
                Request.Method.PUT
        );
    }

    private static void executeRequest(
            Contact contact,
            String url,
            Context context,
            VolleyCallback volleyCallback,
            int method) {

        try {
            RequestQueue queue =
                    Volley.newRequestQueue(context);

            JSONObject object = new JSONObject();
            object.put("id", contact.getContactId());
            object.put("contactName", contact.getContactName());
            object.put("streetAddress", contact.getStreetAddress());
            object.put("city", contact.getCity());
            object.put("state", contact.getState());
            object.put("zipCode", contact.getZipCode());
            object.put("phoneNumber", contact.getPhoneNumber());
            object.put("cellNumber", contact.getCellNumber());
            object.put("email", contact.getEmail());
            object.put(
                    "birthday",
                    String.valueOf(contact.getBirthdayMillis())
            );
            object.put("photo", JSONObject.NULL);
            Bitmap bitmap = contact.getPicture();
            if (bitmap != null) {
                Bitmap scaledBitmap =
                        Bitmap.createScaledBitmap(
                                bitmap, 200, 200, true);

                ByteArrayOutputStream stream =
                        new ByteArrayOutputStream();

                scaledBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        stream
                );
                String jsonPhoto = Base64.encodeToString(
                        stream.toByteArray(),
                        Base64.NO_WRAP
                );
                object.put("photo", jsonPhoto);
            }

            JsonObjectRequest request =
                    new JsonObjectRequest(
                            method,
                            url,
                            object,
                            response -> {

                                Log.d(TAG,
                                        "Save response: " + response);
                                if (response != null) {
                                    int newId = response.optInt(
                                            "id",
                                            contact.getContactId()
                                    );

                                    if (newId > 0) {
                                        contact.setContactId(newId);
                                    }
                                }
                                ArrayList<Contact> result =
                                        new ArrayList<>();

                                result.add(contact);

                                // Notify MainActivity
                                volleyCallback.onSuccess(result);
                            },
                            error -> {
                                Log.e(TAG,
                                        "Save request failed",
                                        error);

                                Toast.makeText(
                                        context,
                                        "Unable to save contact",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                    );

            queue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);

            Toast.makeText(
                    context,
                    "Unable to prepare contact",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public static void executeDeleteRequest(
            Contact contact,
            String url,
            Context context,
            VolleyCallback volleyCallback) {
        Log.d(TAG, "Deleting contact: " + url);

        RequestQueue queue =
                Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d(TAG,
                            "Contact deleted successfully");

                    ArrayList<Contact> result =
                            new ArrayList<>();

                    result.add(contact);

                    volleyCallback.onSuccess(result);
                },
                error -> {

                    Log.e(TAG,
                            "DELETE request failed",
                            error);

                    Toast.makeText(
                            context,
                            "Unable to delete contact",
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
        queue.add(request);

    }
}
