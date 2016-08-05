package com.shopcoup.shareloc;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;


public class UploadAddressService extends IntentService{
    public UploadAddressService() {
        super("UploadAddressService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
        ArrayList<Address> AllAddress = sqLiteHandler.GetNotUpdatedAddresses();

        Iterator<Address> iterator = AllAddress.iterator();
        while (iterator.hasNext()){

            Address tempAddress = iterator.next();
            if(isInternetConnection()) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("mobileNumber", sqLiteHandler.getNumber());
                    jsonObject.put("address",tempAddress.getTextualAddress());
                    jsonObject.put("addressName", tempAddress.getAddressName());
                    jsonObject.put("latitude", tempAddress.getVisualAddressLatitude());
                    jsonObject.put("longitude", tempAddress.getVisualAddressLongitude());
                    jsonObject.put("isPublic", tempAddress.isPublic());
                } catch (JSONException exception) {
                    Log.i("Error", "JSONException - Error in making JSON Object");
                }

                String filename = tempAddress.getAudioFileName();

                if (tempAddress.isSetAudioAddress() && tempAddress.isAudioAddressChanged()) {
                    File audioAddress = new File(tempAddress.getAudioAddress());
                    filename = S3Util.uploadFile(audioAddress);
                    sqLiteHandler.SetAudioFileName(filename, tempAddress.getAddressName());
                }

                try {
                    jsonObject.put("audioLink", filename);
                } catch (JSONException exception) {
                    Log.i("Error", "JSONException - Error in making JSON Object");
                }

                String HostURL = getResources().getString(R.string.HostURL) + "submitLocationForUser";

                if (tempAddress.getUID() != null && !tempAddress.getUID().equals("")) {
                    try {
                        jsonObject.put("uuid", tempAddress.getUID());
                    } catch (JSONException exception) {
                        Log.i("Error", "JSONException - Error in making JSON Object");
                    }
                    HostURL = getResources().getString(R.string.HostURL) + "updateLocationForUser";
                }

                String output = "";
                try {
                    URL url = new URL(HostURL);
                    String outputParameters = jsonObject.toString();
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setFixedLengthStreamingMode(outputParameters.getBytes().length);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                    printWriter.print(outputParameters);
                    printWriter.flush();
                    printWriter.close();

                    Scanner inStream = new Scanner(httpURLConnection.getInputStream());
                    while (inStream.hasNextLine()) {
                        output += inStream.nextLine();
                    }
                    inStream.close();
                    httpURLConnection.disconnect();
                } catch (MalformedURLException exception) {
                    Log.i("Error", "MalformedURLException - Wrong URL");
                } catch (IOException exception) {
                    Log.i("Error", "IOException - Error in Posting Or Input");
                }

                if (tempAddress.getUID() == null || tempAddress.getUID().equals("")) {
                    sqLiteHandler.SetUID(output, tempAddress.getAddressName());
                }

                sqLiteHandler.SetServerUpdatedTrue(tempAddress.getAddressName());
            }
        }
    }

    public boolean isInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            try {
                HttpURLConnection urlc = (HttpURLConnection)(new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Error","IOException - Error Checking Internet");
            }
        }
        return false;
    }
}
