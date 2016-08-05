package com.shopcoup.shareloc;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

public class SearchAddressListAdapter extends ArrayAdapter<JSONObject>{
    boolean isStored = false;
    String CallingActivity;

    public SearchAddressListAdapter(Context context, List<JSONObject> objects , String CallingActivity) {
        super(context, R.layout.row_search_address, objects);
        this.CallingActivity = CallingActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflating Layout Of List
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.row_search_address, parent, false);

        //Get Address Name At The Position
        final JSONObject tempAddress = getItem(position);

        //Setting Address Name
        final String tempAddressName = tempAddress.optString("AddressName");
        TextView AddressName = (TextView) customView.findViewById(R.id.AddressName);
        AddressName.setText(tempAddressName);

        //Set Up Address Persons Number
        String PhoneNumber = tempAddress.optString("PhoneNumber");
        ContactsDB contactsDB = new ContactsDB(getContext());
        final String ContactName = contactsDB.GetContactName(PhoneNumber);
        TextView AddressOf = (TextView) customView.findViewById(R.id.AddressOf);
        if(!CallingActivity.equals("ContactAddresses")) {
            if (ContactName != null)
                AddressOf.setText("Address Of: " + ContactName);
            else
                AddressOf.setText("Address Of: " + PhoneNumber);
        }else{
            AddressOf.setVisibility(View.GONE);
        }
        final String UID = tempAddress.optString("uuid");

        //Set On Click Event For The ImageView
        ImageView ExpandAddress = (ImageView) customView.findViewById(R.id.ExpandAddress);
        ExpandAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewAddress.class);
                intent.putExtra("AddressName", tempAddressName);
                intent.putExtra("uuid",UID);
                intent.putExtra("CallingActivity",CallingActivity);
                getContext().startActivity(intent);
            }
        });

        //Set Public Button
        final View AddToMyAddressButton = customView.findViewById(R.id.AddToMyAddressButton);
        final TextView AddToMyAddressButtonText = (TextView) customView.findViewById(R.id.AddToMyAddressButtonText);
        final ImageView AddToMyAddressButtonImage = (ImageView) customView.findViewById(R.id.AddToMyAddressButtonImage);
        final SQLiteHandler sqLiteHandler = new SQLiteHandler(getContext());
        isStored = sqLiteHandler.GetRecentAddressStored(UID);
        if(!isStored) {
            AddToMyAddressButtonText.setText("Add To My Address");
            AddToMyAddressButtonImage.setImageResource(R.drawable.ic_person_add_black_36dp);
        }else{
            AddToMyAddressButton.setVisibility(View.GONE);
        }
        AddToMyAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStored = true;
                Address address = sqLiteHandler.getRecentAddressOfName(tempAddressName, UID);
                address.setAudioAddressChanged(false);
                sqLiteHandler.addAddress(address);
                Intent intent = new Intent(getContext(),UploadAddressService.class);
                getContext().startService(intent);
                view.setVisibility(View.GONE);
            }
        });

        return customView;
    }
}
