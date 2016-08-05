package com.shopcoup.shareloc;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallBarringActivity extends AppCompatActivity {

    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if(Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Intent intent = new Intent(this, CallBarring.class);
                    intent.putExtra("CheckedPermission", true);
                    intent.setAction(getIntent().getAction());
                    intent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, getIntent().getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
                    intent.putExtra(Intent.EXTRA_PHONE_NUMBER,getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER));
                    sendBroadcast(intent);
                    finish();
                }
            }
        }
    }
}
