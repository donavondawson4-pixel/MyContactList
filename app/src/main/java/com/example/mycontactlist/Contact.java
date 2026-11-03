package com.example.mycontactlist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

public class Contact {
    private int contactId = -1;
    private String contactName = "";
    private String streetAddress = "";
    private String city = "";
    private String state = "";
    private String zipCode = "";
    private String phoneNumber = "";
    private String cellNumber = "";
    private String email = "";
    private long birthdayMillis = System.currentTimeMillis();



    private byte[] contactPhoto;


    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCellNumber() {
        return cellNumber;
    }

    public void setCellNumber(String cellNumber) {
        this.cellNumber = cellNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getBirthdayMillis() {
        return birthdayMillis;
    }

    public void setBirthdayMillis(long birthdayMillis) {
        this.birthdayMillis = birthdayMillis;
    }

    public byte[] getContactPhoto() {
        return contactPhoto;
    }

    public void setContactPhoto(byte[] contactPhoto) {
        this.contactPhoto = contactPhoto;
    }


    public Bitmap getPicture() {
        if (contactPhoto == null || contactPhoto.length == 0) {
            return null;
        }

        return BitmapFactory.decodeByteArray(
                contactPhoto, 0, contactPhoto.length);
    }

    public void setPicture(Bitmap picture) {
        if (picture == null) {
            contactPhoto = null;
            return;
        }

        ByteArrayOutputStream stream =
                new ByteArrayOutputStream();

        picture.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                stream);

        contactPhoto = stream.toByteArray();
    }


}