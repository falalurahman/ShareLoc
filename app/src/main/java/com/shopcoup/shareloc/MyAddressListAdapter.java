package com.shopcoup.shareloc;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class MyAddressListAdapter extends ArrayAdapter<JSONObject> {

    boolean isPublic = false;


    public MyAddressListAdapter(Context context, ArrayList<JSONObject> objects) {
        super(context, R.layout.row_myaddress_list, objects);
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        //Inflating Layout Of List
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View customView = layoutInflater.inflate(R.layout.row_myaddress_list, parent, false);

        //Get Address Name At The Position
        final JSONObject tempAddress = getItem(position);

        final String tempAddressName = tempAddress.optString("AddressName");

        //Setting Address Name
        TextView AddressName = (TextView) customView.findViewById(R.id.AddressName);
        AddressName.setText(tempAddressName);

        final String PhoneNumber = tempAddress.optString("PhoneNumber");

        final SQLiteHandler sqLiteHandler = new SQLiteHandler(getContext());

        if(!PhoneNumber.equals(sqLiteHandler.getNumber())){
            TextView AddressOf = (TextView) customView.findViewById(R.id.AddressOf);
            ContactsDB contactsDB = new ContactsDB(getContext());
            final String ContactName = contactsDB.GetContactName(PhoneNumber);
            if(ContactName != null)
                AddressOf.setText("Address Of: " + ContactName);
            else
                AddressOf.setText("Address Of: " + PhoneNumber);
            AddressOf.setVisibility(View.VISIBLE);
        }

        final String UID = tempAddress.optString("uuid");
        //Set On Click Event For The ImageView
        ImageView ExpandAddress = (ImageView) customView.findViewById(R.id.ExpandAddress);
        ExpandAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditAddress.class);
                intent.putExtra("AddressName", tempAddressName);
                intent.putExtra("uuid",UID);
                getContext().startActivity(intent);
            }
        });

        //Set Public Button
        final View SetPublicButton = customView.findViewById(R.id.SetPublicButton);
        final TextView PublicButtonText = (TextView) customView.findViewById(R.id.PublicButtonText);
        final ImageView PublicButtonImage = (ImageView) customView.findViewById(R.id.PublicButtonImage);
        isPublic = sqLiteHandler.GetAddressIsPublic(tempAddressName);
        if(isPublic) {
            PublicButtonText.setText("Public");
            PublicButtonImage.setImageResource(R.drawable.ic_public_black_36dp);
        }
        else {
            PublicButtonText.setText("Private");
            PublicButtonImage.setImageResource(R.drawable.ic_person_black_36dp);
        }
        SetPublicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPublic) {
                    PublicButtonText.setText("Private");
                    PublicButtonImage.setImageResource(R.drawable.ic_person_black_36dp);
                    isPublic = false;
                }
                else {
                    PublicButtonText.setText("Public");
                    PublicButtonImage.setImageResource(R.drawable.ic_public_black_36dp);
                    isPublic = true;
                }
                sqLiteHandler.toggleIsPublic(tempAddressName);
                Intent serviceIntent = new Intent(getContext(),UploadAddressService.class);
                getContext().startService(serviceIntent);
            }
        });

        final View ShareButton = customView.findViewById(R.id.ShareButton);
        ShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "shareloc.in/getuserlocationbyid?addressUID=" + UID);
                sendIntent.setType("text/plain");
                getContext().startActivity(sendIntent);
            }
        });

        //Animation When Creating View
        /*ScaleAnimation scaleAnimation =
                new ScaleAnimation(0.0f,1.0f,0.0f,1.0f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        AlphaAnimation alphaAnimation =
                new AlphaAnimation(0.0f,1.0f);
        final AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(400);
        customView.startAnimation(animationSet);*/

        return customView;
    }
}