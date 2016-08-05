package com.shopcoup.shareloc;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Error", "Reached receiver");
        Intent serviceIntent = new Intent(context,UploadAddressService.class);
        context.startService(serviceIntent);
        SharedPreferences sharedPreferences = context.getSharedPreferences("ShareLoc",Context.MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("ContactsUpdatedOnce",false)){
            Intent serviceIntent2 = new Intent(context,UpdateContactAddress.class);
            context.startService(serviceIntent2);
        }
    }
}
