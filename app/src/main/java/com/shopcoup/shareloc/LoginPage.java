package com.shopcoup.shareloc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginPage extends AppCompatActivity {

    ProgressDialog progressDialog;

    SQLiteHandler sqLiteHandler = null;
    String PhoneNumber;
    boolean CompletedDownloading[];
    boolean reachedOTP = false;

    String ServerOTP,PhoneOTP;

    Tracker mTracker;

    View loginForm;
    View otpForm;

    Button button1;

    IntentFilter intentFilter;
    SMSReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        BaseClass application = (BaseClass) getApplication();
        mTracker = application.getDefaultTracker();

        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                mTracker,
                Thread.getDefaultUncaughtExceptionHandler(),
                LoginPage.this);
        Thread.setDefaultUncaughtExceptionHandler(myHandler);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent serviceIntent = new Intent(this,UploadAddressService.class);
        startService(serviceIntent);

        //Check if already logged in
        sqLiteHandler = new SQLiteHandler(getApplicationContext());
        if(sqLiteHandler.hasNumber()) {
            //Going to Main Activity
            Intent intent = new Intent(LoginPage.this , MainActivity.class);
            startActivity(intent);
            finish();
        }

        otpForm = findViewById(R.id.otpform);
        loginForm = findViewById(R.id.loginForm);
        button1 = (Button) findViewById(R.id.button1);

        intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        //intentFilter = new IntentFilter("android.intent.action.DATA_SMS_RECEIVED");
        intentFilter.setPriority(10);
        //intentFilter.addDataScheme("sms");
        //intentFilter.addDataAuthority("*", "6734");
        smsReceiver = new SMSReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Login Page");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    //Setting up onclick function for submit button
    public void onClickSubmit(View view){

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Login Using A Number")
                .build());

        EditText editText = (EditText) findViewById(R.id.editText);

        PhoneNumber= editText.getText().toString();

        if(PhoneNumber.length() < 10){
            editText.setError("Incorrect Phone Number");
            return;
        }

        if(PhoneNumber.contains("+91"))
            PhoneNumber = PhoneNumber.replace("+91","");
        if(PhoneNumber.contains("+91-"))
            PhoneNumber = PhoneNumber.replace("+91-","");
        if(PhoneNumber.startsWith("0"))
            PhoneNumber = PhoneNumber.substring(1);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.number_confirm_dialog);

        TextView DialogNumber = (TextView) dialog.findViewById(R.id.DialogNumber);
        DialogNumber.setText(PhoneNumber);
        TextView Confirm = (TextView) dialog.findViewById(R.id.Confirm);
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                DownloadOTP();
            }
        });
        TextView Edit = (TextView) dialog.findViewById(R.id.Edit);
        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public AnimationSet fadeout(final View view){
        ScaleAnimation scaleAnimation =
                new ScaleAnimation(1.0f,0.0f,1.0f,0.0f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        AlphaAnimation alphaAnimation =
                new AlphaAnimation(1.0f,0.0f);
        final AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(400);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return animationSet;
    }

    public AnimationSet fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        ScaleAnimation scaleAnimation =
                new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation =
                new AlphaAnimation(0.0f, 1.0f);
        final AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(400);
        return animationSet;
    }

    public void animateOTPForm(){

        AnimationSet loginanimationSet = fadeout(loginForm);
        AnimationSet otpanimationSet = fadeIn(otpForm);

        button1.setEnabled(false);
        loginForm.startAnimation(loginanimationSet);
        otpForm.startAnimation(otpanimationSet);
        button1.setTextColor(getResources().getColor(R.color.colorTertiaryText));

        new CountDownTimer(60000,500){
            @Override
            public void onTick(long millisUntilFinished) {
                button1.setText("Try again in " + Long.toString(millisUntilFinished/1000) + "s");
            }

            @Override
            public void onFinish() {
                button1.setTextColor(getResources().getColor(R.color.colorTextOnPrimaryColor));
                button1.setText("Submit OTP");
                button1.setEnabled(true);
            }
        }.start();

        registerReceiver(smsReceiver,intentFilter);
    }

    public void SubmitOTP(View view){
        EditText OTP = (EditText) findViewById(R.id.otp);
        PhoneOTP = OTP.getText().toString();

        if(!PhoneOTP.equals(ServerOTP))
            return;

        AnimationSet otpanimationSet = fadeout(otpForm);
        otpForm.startAnimation(otpanimationSet);
        unregisterReceiver(smsReceiver);
        DownloadAddress();
    }

    public void deAnimateOTPForm() {

        AnimationSet otpanimationSet = fadeout(otpForm);
        AnimationSet loginanimationSet = fadeIn(loginForm);


        otpForm.startAnimation(otpanimationSet);
        loginForm.startAnimation(loginanimationSet);

    }

    public void DownloadOTP(){
        final AsyncTask<Void,Void,Boolean> OTPDownloader = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                String output="";
                try {
                    String HostURL = getResources().getString(R.string.HostURL) + "loginNumber?mobileNumber=" + PhoneNumber;
                    URL url = new URL(HostURL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    Scanner inStream = new Scanner(httpURLConnection.getInputStream());
                    while (inStream.hasNextLine()) {
                        output += inStream.nextLine();
                    }
                    inStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException exception) {
                    Log.i("Error", "MalformedURLException - Wrong URL");
                    return false;
                } catch (IOException exception) {
                    Log.i("Error", "IOException - Error in Posting Or Input");
                    return false;
                }
                ServerOTP = output;
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
                progressDialog = null;
                if(!success){
                    final Dialog dialog = new Dialog(LoginPage.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert_dialog);

                    TextView DialogTitle = (TextView) dialog.findViewById(R.id.DialogTitle);
                    TextView DialogMessage = (TextView) dialog.findViewById(R.id.DialogMessage);
                    TextView Ok = (TextView) dialog.findViewById(R.id.Ok);
                    DialogTitle.setText("Sorry");
                    DialogMessage.setText("Login Error");
                    Ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }else{
                    reachedOTP = true;
                    animateOTPForm();
                }
            }
        };

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar_drawable));
        progressDialog.setMessage("Verifying Phone Number...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                OTPDownloader.cancel(true);
            }
        });

        OTPDownloader.execute((Void) null);
    }

    public void DownloadAddress(){
        //Adding number to SQLite Database
        sqLiteHandler.addNumber(PhoneNumber);

        //Downloading All Addresses

        final SQLiteHandler sqLiteHandler = new SQLiteHandler(this);

        final AsyncTask<Void,Void,Boolean> AddressDownloader = new AsyncTask<Void,Void,Boolean>() {
            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                String output="";
                try {
                    String HostURL = getResources().getString(R.string.HostURL) + "getuserlocationdata?mobile=" + PhoneNumber;
                    URL url = new URL(HostURL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    Scanner inStream = new Scanner(httpURLConnection.getInputStream());
                    while (inStream.hasNextLine()) {
                        output += inStream.nextLine();
                    }
                    inStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException exception) {
                    Log.i("Error", "MalformedURLException - Wrong URL");
                    return false;
                } catch (IOException exception) {
                    Log.i("Error", "IOException - Error in Posting Or Input");
                    return false;
                }
                if(!output.equals("")) {
                    try {
                        final JSONArray jsonArray = new JSONArray(output);
                        if(jsonArray.length() == 0){
                            //Going To Main Activity
                            Intent intent = new Intent(LoginPage.this , MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        sqLiteHandler.clearNowAddress();
                        int i = 0;
                        CompletedDownloading = new boolean[jsonArray.length()];
                        while (i < jsonArray.length()){
                            final Address tempAddress = new Address();
                            final int position = i;
                            CompletedDownloading[position] = false;
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            i++;
                            tempAddress.setAddressName(jsonObject.optString("addressName"));
                            tempAddress.setIsPublic(Boolean.parseBoolean(jsonObject.optString("isPublic")));
                            tempAddress.setPhoneNumber(jsonObject.optString("mobileNumber"));
                            tempAddress.setTextualAddress(jsonObject.optString("address"));
                            tempAddress.setVisualAddressLatitude(Double.parseDouble(jsonObject.optString("latitude")));
                            tempAddress.setVisualAddressLongitude(Double.parseDouble(jsonObject.optString("longitude")));
                            tempAddress.setAudioFileName(jsonObject.optString("audioLink"));
                            tempAddress.setUID(jsonObject.optString("uuid"));
                            String timeStamp = String.valueOf(System.currentTimeMillis());
                            String username = tempAddress.getPhoneNumber();
                            final String Filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecording_" + username +
                                    "_"+timeStamp +  ".3gp";
                            File DownloadedFile = new File(Filename);
                            Download downloaded = S3Util.download(tempAddress.getAudioFileName(),DownloadedFile);
                            if(downloaded != null) {
                                downloaded.addProgressListener(new ProgressListener() {
                                    @Override
                                    public void progressChanged(ProgressEvent progressEvent) {
                                        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                                            tempAddress.setIsSetAudioAddress(true);
                                            tempAddress.setAudioAddress(Filename);
                                            tempAddress.setAudioAddressChanged(false);
                                            sqLiteHandler.addAddress(tempAddress);
                                            CompletedDownloading[position] = true;

                                            boolean checking = true;
                                            for(int j=0;j<jsonArray.length();j++){
                                                if(!CompletedDownloading[j]){
                                                    checking = false;
                                                    break;
                                                }
                                            }

                                            if (checking) {
                                                progressDialog.dismiss();
                                                progressDialog = null;
                                                Intent intent = new Intent(LoginPage.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    }
                                });
                            }else{
                                tempAddress.setIsSetAudioAddress(false);
                                tempAddress.setAudioAddress(null);
                                sqLiteHandler.addAddress(tempAddress);
                                CompletedDownloading[position] = true;

                                boolean checking = true;
                                for(int j=0;j<jsonArray.length();j++){
                                    if(!CompletedDownloading[j]){
                                        checking = false;
                                        break;
                                    }
                                }

                                if (checking) {
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                    Intent intent = new Intent(LoginPage.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    }catch (JSONException jsonException){
                        return false;
                    }
                }else{
                    Intent intent = new Intent(LoginPage.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }


            @Override
            protected void onPostExecute(Boolean success) {
                if(!success){
                    progressDialog.dismiss();
                    progressDialog = null;
                    final Dialog dialog = new Dialog(LoginPage.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert_dialog);

                    TextView DialogTitle = (TextView) dialog.findViewById(R.id.DialogTitle);
                    TextView DialogMessage = (TextView) dialog.findViewById(R.id.DialogMessage);
                    TextView Ok = (TextView) dialog.findViewById(R.id.Ok);
                    DialogTitle.setText("Sorry");
                    DialogMessage.setText("Login Error");
                    Ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    AnimationSet loginanimationSet = fadeIn(loginForm);
                    loginForm.startAnimation(loginanimationSet);
                    reachedOTP = false;
                }
            }
        };

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar_drawable));
        progressDialog.setMessage("Downloading Address...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                AddressDownloader.cancel(true);
                AnimationSet loginanimationSet = fadeIn(loginForm);
                loginForm.startAnimation(loginanimationSet);
                reachedOTP = false;
            }
        });

        AddressDownloader.execute((Void) null);
    }

    @Override
    protected void onPause() {
        if(progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
        super.onPause();
    }

    public class SMSReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Error","Reached Receiver");
            Bundle bundle = intent.getExtras();
            if(bundle == null)
                return;

            Object pdus[] = (Object[]) bundle.get("pdus");
            for(int i=0; i < pdus.length; i++){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String sender = smsMessage.getOriginatingAddress();
                String body = smsMessage.getMessageBody().toString();
                if(sender.equals("MM-SHRLOC")){
                    if(body.contains(ServerOTP)){
                        AnimationSet otpanimationSet = fadeout(otpForm);
                        otpForm.startAnimation(otpanimationSet);
                        unregisterReceiver(smsReceiver);
                        DownloadAddress();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(reachedOTP){
            final Dialog dialog = new Dialog(LoginPage.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.two_switch_dialog);

            TextView Yes = (TextView) dialog.findViewById(R.id.Yes);
            Yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    unregisterReceiver(smsReceiver);
                    deAnimateOTPForm();
                    reachedOTP = false;
                }
            });
            TextView No = (TextView) dialog.findViewById(R.id.No);
            No.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }else {
            super.onBackPressed();
        }
    }
}
