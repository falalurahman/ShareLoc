package com.shopcoup.shareloc;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ContactAddresses extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_addresses);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String ContactName = getIntent().getStringExtra("ContactName");
        String PhoneNumber = getIntent().getStringExtra("PhoneNumber");
        setTitle(ContactName + " Addresses");

        ContactsDB contactsDB = new ContactsDB(this);
        ArrayList<JSONObject> contactAddresses =  contactsDB.GetAddressOfPhoneNumber(PhoneNumber);

        ListView SearchAddressListView = (ListView) findViewById(R.id.SearchAddressListView);
        SearchAddressListAdapter listAdapter = new SearchAddressListAdapter(this,contactAddresses,"ContactAddresses");
        SearchAddressListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(ContactAddresses.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("ContactAddresses",true);
            startActivity(intent);
            finish();
        }
        return true;
    }
}
