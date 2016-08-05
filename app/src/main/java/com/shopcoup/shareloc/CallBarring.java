package com.shopcoup.shareloc;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONObject;

import java.util.ArrayList;

public class CallBarring extends BroadcastReceiver{

    FABDialog fabDialog;

    String CallerNumber = null;

    Tracker myTracker;

    @Override
    public void onReceive(Context context,final Intent intent) {

        BaseClass application = (BaseClass) context.getApplicationContext();
        myTracker = application.getDefaultTracker();

        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                myTracker,
                Thread.getDefaultUncaughtExceptionHandler(),
                context);
        Thread.setDefaultUncaughtExceptionHandler(myHandler);

        if (!(intent.getAction().equals("android.intent.action.PHONE_STATE") && intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) != null)
                && !intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
            return;

        boolean checkedPermission = intent.getBooleanExtra("CheckedPermission", false);

        if (Build.VERSION.SDK_INT >= 23 && !checkedPermission) {
            if (!Settings.canDrawOverlays(context)) {
                Intent permissionIntent = new Intent(context, CallBarringActivity.class);
                permissionIntent.setAction(intent.getAction());
                permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                permissionIntent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
                permissionIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
                context.startActivity(permissionIntent);
            } else {
                checkedPermission = true;
            }
        } else {
            checkedPermission = true;
        }
        if (checkedPermission) {
            if (fabDialog == null) {
                fabDialog = new FABDialog(context);

                fabDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                Window window = fabDialog.getWindow();

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);

                params.gravity = Gravity.TOP | Gravity.RIGHT;
                params.x = 50;
                params.y = 50;

                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setAttributes(params);

                fabDialog.setCancelable(false);
                fabDialog.show();

            }

            // Fetch the number of incoming call
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
                CallerNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            else
                CallerNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            fabDialog.dismiss();
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            fabDialog.show();
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            fabDialog.show();
                            break;
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);

        }
    }

    class FABDialog extends Dialog{

        CustomDialog customDialog = null;

        public FABDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.call_barring_layout);

            ImageView callFAB = (ImageView) findViewById(R.id.callFAB);
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("ShareLocTutorial",Context.MODE_PRIVATE);
            if(!sharedPreferences.getBoolean("SentSms",false)){
                final ImageView arrow = (ImageView) findViewById(R.id.arrow);
                final TextView message = (TextView) findViewById(R.id.message);
                arrow.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("SentSms",true);
                editor.apply();
                message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        arrow.setVisibility(View.GONE);
                        message.setVisibility(View.GONE);
                    }
                });
            }
            callFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (customDialog == null) {
                        customDialog = new CustomDialog(getContext());

                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        Window window = customDialog.getWindow();

                        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.TYPE_PHONE,
                                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                                PixelFormat.TRANSLUCENT);

                        params.gravity = Gravity.CENTER;

                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        window.setAttributes(params);

                        customDialog.setCancelable(false);
                    }
                    customDialog.show();
                }
            });
        }

        public int convertdptopx(int dp) {
            Resources resources = getContext().getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
            return px;
        }

        @Override
        public void setOnDismissListener(OnDismissListener listener) {
            super.setOnDismissListener(listener);
            if(customDialog != null)
                customDialog.dismiss();
        }
    }

    class CustomDialog extends Dialog{
        public CustomDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.call_barring_my_address_dialog_layout);

            SQLiteHandler sqLiteHandler = new SQLiteHandler(getContext());
            final ArrayList<JSONObject> MyAddresses = sqLiteHandler.GetAllOnceUpdatedAddressNames();
            ListView MyAddressListView = (ListView) findViewById(R.id.MyAddressListView);
            MyCallAddressListAdapter listAdapter = new MyCallAddressListAdapter(getContext(),MyAddresses);
            MyAddressListView.setAdapter(listAdapter);
            MyAddressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    JSONObject jsonObject = MyAddresses.get(position);
                    String UID = jsonObject.optString("uuid");
                    String Message = "shareloc.in/getuserlocationbyid?addressUID=" + UID;
                    myTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Action")
                            .setAction("Share Location During Call")
                            .build());
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(CallerNumber, null, Message, null, null);
                        Toast.makeText(getContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });

            ImageButton CloseButton = (ImageButton) findViewById(R.id.CloseButton);
            CloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
    }

    public class MyCallAddressListAdapter extends ArrayAdapter<JSONObject>{
        public MyCallAddressListAdapter(Context context,ArrayList<JSONObject> objects) {
            super(context, R.layout.row_call_barring_my_address, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Inflating Layout Of List
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            final View customView = layoutInflater.inflate(R.layout.row_call_barring_my_address, parent, false);

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

            return customView;
        }
    }
}
