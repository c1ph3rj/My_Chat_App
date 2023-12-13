package com.codinginflow.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactsScreen extends AppCompatActivity implements FirebaseHelper.UserDetailsCallback {
    List<UserDetails> listOfUsers;
    CustomProgressDialog progressDialog;
    RecyclerView contactsView;
    ImageView backBtn;

    @SuppressLint("Range")
    public static ArrayList<ContactDetails> getAllContacts(Context context) {
        ArrayList<ContactDetails> contactList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Projection for the columns to retrieve
        String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        // Query to retrieve contacts
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Get contact details
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                // Create ContactDetails object and add to the list

                ContactDetails contactDetails = new ContactDetails();
                contactDetails.mobileNumber = phoneNumber;
                contactDetails.contactName = contactName;
                contactDetails.contactId = contactId;
                contactList.add(contactDetails);
            }
            cursor.close();
        }

        return contactList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_screen);

        getSupportActionBar().hide();
        listOfUsers = new ArrayList<>();

        init();


    }

    void initContactsView() {
        try {
            ContactsViewAdapter contactsViewAdapter = new ContactsViewAdapter(listOfUsers, this);
            contactsView.setAdapter(contactsViewAdapter);
            contactsView.setLayoutManager(new LinearLayoutManager(this));

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }

    void init() {
        try {
            progressDialog = new CustomProgressDialog(this);
            contactsView = findViewById(R.id.contactsView);
            backBtn = findViewById(R.id.backBtn);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<ContactDetails> allContacts = getAllContacts(ContactsScreen.this);
                    FirebaseHelper firebaseHelper = new FirebaseHelper();
                    firebaseHelper.checkAndStoreUserDetails(allContacts, ContactsScreen.this);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }

    @Override
    public void onUserDetailsFetched(List<UserDetails> userDetailsList) {
        this.listOfUsers = userDetailsList;
        initContactsView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}