package com.shopcoup.shareloc;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.Manifest;

public class LandingPage extends AppCompatActivity {

    final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 111;
    final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 112;
    final int MY_PERMISSIONS_READ_PHONE_STATE = 113;
    final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 114;
    final int MY_PERMISSIONS_ACCESS_FINE_LOCATIONS = 115;
    final int MY_PERMISSIONS_RECORD_AUDIO = 116;
    final int MY_PERMISSIONS_READ_CONTACTS = 117;
    final int MY_PERMISSIONS_PROCESS_OUTGOING_CALL = 118;
    final int MY_PERMISSIONS_SEND_SMS = 119;
    final int MY_PERMISSIONS_RECEIVE_SMS = 122;
    final int MY_PERMISSIONS_SYSTEM_OVERLAY = 120;
    final int MY_PERMISSIONS_CALL_PHONE = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        try {
            getSupportActionBar().hide();
        }catch (NullPointerException exception){
            Log.i("Errors","Null Pointer Exception - Cannot Hide Action Bar ");
        }



        //Handler For Sending intent after the thread is executed
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                checkPermission();
            }
        };

        //Waiting For 3 seconds in another thread
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long futureTime = System.currentTimeMillis() + 500;
                while (System.currentTimeMillis() < futureTime){
                    synchronized (this){
                        try{
                            wait(futureTime - System.currentTimeMillis());
                        }catch (Exception exception){
                            Log.i("Errors","Error In Waiting");
                        }
                    }
                }
                handler.sendEmptyMessage(0);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    public void body(){
        //Handler For Sending intent after the thread is executed
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent(LandingPage.this , LoginPage.class);
                startActivity(intent);
                finish();
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ContactsDB contactsDB = new ContactsDB(LandingPage.this);
                contactsDB.Upgrade(LandingPage.this);
                handler.sendEmptyMessage(0);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_READ_PHONE_STATE);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATIONS);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_READ_CONTACTS);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.PROCESS_OUTGOING_CALLS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.PROCESS_OUTGOING_CALLS},
                    MY_PERMISSIONS_PROCESS_OUTGOING_CALL);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_SEND_SMS);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_RECEIVE_SMS);
        }else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_CALL_PHONE);
        }else if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, MY_PERMISSIONS_SYSTEM_OVERLAY);
            }else {
                body();
            }
        } else{
            body();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to WRITE to EXTERNAL STORAGE is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to READ from EXTERNAL STORAGE is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_READ_PHONE_STATE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to READ PHONE STATE is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to ACCESS LOCATION is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_ACCESS_FINE_LOCATIONS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to ACCESS LOCATION is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_RECORD_AUDIO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to RECORD AUDIO is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_READ_CONTACTS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to READ CONTACTS is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_PROCESS_OUTGOING_CALL:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to ACCESS OUTGOING CALLS is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_SEND_SMS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to SEND SMS is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_RECEIVE_SMS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to RECEIVE SMS is required to run this application. Please grant this permission.");
                }
                break;
            case MY_PERMISSIONS_CALL_PHONE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }else{
                    showDialog("Permission to CALL PHONE is required to run this application. Please grant this permission.");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MY_PERMISSIONS_SYSTEM_OVERLAY) {
            if (Build.VERSION.SDK_INT >= 23) {
                if(!Settings.canDrawOverlays(this)){
                    checkPermission();
                }else {
                    showDialog("Permission for SYSTEM OVERLAY is required to run this application. Please grant this permission.");
                    checkPermission();
                }
            } else {
                checkPermission();
            }
        }
    }

    public void showDialog( String message ){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog);

        TextView DialogTitle = (TextView) dialog.findViewById(R.id.DialogTitle);
        TextView DialogMessage = (TextView) dialog.findViewById(R.id.DialogMessage);
        TextView Ok = (TextView) dialog.findViewById(R.id.Ok);
        DialogTitle.setText("Permission Denied");
        DialogMessage.setText(message);
        Ok.setText("RETRY");
        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                checkPermission();
            }
        });
        dialog.show();
    }
}
