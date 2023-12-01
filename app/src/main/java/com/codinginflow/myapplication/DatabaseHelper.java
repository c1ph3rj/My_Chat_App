package com.codinginflow.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_details_db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGE_DETAILS = "message_details";
    private static final String KEY_ID = "id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_PROFILE_PIC = "profile_pic";
    private static final String KEY_ABOUT_DETAILS = "about_details";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_DETAILS_TABLE = "CREATE TABLE " + TABLE_MESSAGE_DETAILS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_NAME + " TEXT,"
                + KEY_UUID + " TEXT,"
                + KEY_PHONE_NUMBER + " TEXT,"
                + KEY_PROFILE_PIC + " TEXT,"
                + KEY_ABOUT_DETAILS + " TEXT" + ")";
        db.execSQL(CREATE_USER_DETAILS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE_DETAILS);
        onCreate(db);
    }

    // Method to add a single UserDetails to the database
    public void addUserDetails(UserDetails userDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, userDetails.userName);
        values.put(KEY_UUID, userDetails.uuid);
        values.put(KEY_PHONE_NUMBER, userDetails.phoneNumber);
        values.put(KEY_PROFILE_PIC, userDetails.profilePic);
        values.put(KEY_ABOUT_DETAILS, userDetails.aboutDetails);

        db.insert(TABLE_MESSAGE_DETAILS, null, values);
        db.close();
    }

    // Method to add a list of UserDetails to the database
    public void addListOfUserDetails(List<UserDetails> userDetailsList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (UserDetails userDetails : userDetailsList) {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_NAME, userDetails.userName);
            values.put(KEY_UUID, userDetails.uuid);
            values.put(KEY_PHONE_NUMBER, userDetails.phoneNumber);
            values.put(KEY_PROFILE_PIC, userDetails.profilePic);
            values.put(KEY_ABOUT_DETAILS, userDetails.aboutDetails);

            db.insert(TABLE_MESSAGE_DETAILS, null, values);
        }
        db.close();
    }

    // Method to get all UserDetails from the database
    @SuppressLint("Range")
    public ArrayList<UserDetails> getAllMessageDetails() {
        ArrayList<UserDetails> userDetailsList = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGE_DETAILS, null);

            if (cursor.moveToFirst()) {
                do {
                    UserDetails userDetails = new UserDetails();
                    userDetails.userName = (cursor.getString(cursor.getColumnIndex(KEY_USER_NAME)));
                    userDetails.uuid = (cursor.getString(cursor.getColumnIndex(KEY_UUID)));
                    userDetails.phoneNumber = (cursor.getString(cursor.getColumnIndex(KEY_PHONE_NUMBER)));
                    userDetails.profilePic = (cursor.getString(cursor.getColumnIndex(KEY_PROFILE_PIC)));
                    userDetails.aboutDetails = (cursor.getString(cursor.getColumnIndex(KEY_ABOUT_DETAILS)));

                    userDetailsList.add(userDetails);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userDetailsList;
    }

    // Method to update a single UserDetails in the database
    public void updateUserDetails(UserDetails userDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, userDetails.userName);
        values.put(KEY_PHONE_NUMBER, userDetails.phoneNumber);
        values.put(KEY_PROFILE_PIC, userDetails.profilePic);
        values.put(KEY_ABOUT_DETAILS, userDetails.aboutDetails);

        db.update(TABLE_MESSAGE_DETAILS, values, KEY_UUID + " = ?", new String[]{userDetails.uuid});
        db.close();
    }

    // Method to update a list of UserDetails in the database
    public void updateListOfUserDetails(List<UserDetails> userDetailsList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (UserDetails userDetails : userDetailsList) {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_NAME, userDetails.userName);
            values.put(KEY_PHONE_NUMBER, userDetails.phoneNumber);
            values.put(KEY_PROFILE_PIC, userDetails.profilePic);
            values.put(KEY_ABOUT_DETAILS, userDetails.aboutDetails);

            db.update(TABLE_MESSAGE_DETAILS, values, KEY_UUID + " = ?", new String[]{userDetails.uuid});
        }
        db.close();
    }

    // Method to delete a single UserDetails from the database
    public void deleteUserDetails(UserDetails userDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGE_DETAILS, KEY_UUID + " = ?", new String[]{userDetails.uuid});
        db.close();
    }

    // Method to delete the entire list of UserDetails from the database
    public void deleteAllUserDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MESSAGE_DETAILS);
        db.close();
    }
}

