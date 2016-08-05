package com.shopcoup.shareloc;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.Download;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class UpdateContactAddress extends IntentService{
    public UpdateContactAddress() {
            super("UpdateContactAddress");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ContactsDB contactsDB = new ContactsDB(this);
        ArrayList<Contacts> AllContacts = contactsDB.GetAllContacts();
        final SQLiteDatabase sqLiteDatabase = contactsDB.GetDatabase();
        int i = 0, limit = 20;
        while (isInternetConnection() && i < AllContacts.size()){
            JSONArray PhoneNumberArray = new JSONArray();
            for (;i < limit && i < AllContacts.size();i++){
                Contacts contacts = AllContacts.get(i);
                String PhoneNumber = contacts.getPhoneNumber();
                /*if(PhoneNumber.contains("+91-"))
                    PhoneNumber = PhoneNumber.replace("+91-","");
                if(PhoneNumber.contains("+91"))
                    PhoneNumber = PhoneNumber.replace("+91","");
                if(PhoneNumber.startsWith("0"))
                    PhoneNumber = PhoneNumber.substring(1);*/
                PhoneNumberArray.put(PhoneNumber);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("contacts",PhoneNumberArray);
            }catch (JSONException exception){
                Log.i("Error","JSONException - Error In JSON");
            }

            String output = "";
            try{
                String HostURL = getResources().getString(R.string.HostURL) + "synccontactslocation";
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

            if(!output.equals("")) {
                try {
                    JSONArray jsonArray = new JSONArray(output);
                    int j = 0;
                    while (j < jsonArray.length()) {
                        JSONObject jsonRootObject = jsonArray.getJSONObject(j);
                        String MobileNumber = jsonRootObject.optString("mobileNumber");
                        JSONArray AddressArray = jsonRootObject.optJSONArray("locationAddresses");

                        int k = 0;
                        while (k < AddressArray.length()) {
                            final Address tempAddress = new Address();
                            JSONObject jsonAddress = AddressArray.getJSONObject(k);
                            tempAddress.setAddressName(jsonAddress.optString("addressName"));
                            tempAddress.setIsPublic(Boolean.parseBoolean(jsonAddress.optString("isPublic")));
                            tempAddress.setPhoneNumber(MobileNumber);
                            tempAddress.setTextualAddress(jsonAddress.optString("address"));
                            tempAddress.setVisualAddressLatitude(Double.parseDouble(jsonAddress.optString("latitude")));
                            tempAddress.setVisualAddressLongitude(Double.parseDouble(jsonAddress.optString("longitude")));
                            tempAddress.setAudioFileName(jsonAddress.optString("audioLink"));
                            tempAddress.setUID(jsonAddress.optString("uuid"));

                            ContentValues contentValues = contactsDB.GetContactAddressAudio(tempAddress.getUID(),sqLiteDatabase);
                            if (tempAddress.getAudioFileName() != null && !tempAddress.getAudioFileName().equals("")) {
                                if (contentValues != null && contentValues.getAsBoolean("IsAudioAddress")) {
                                    if (contentValues.getAsString("AudioFilename").equals(tempAddress.getAudioFileName())) {
                                        tempAddress.setIsSetAudioAddress(true);
                                        tempAddress.setAudioAddress(contentValues.getAsString("AudioAddressLocation"));
                                        contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
                                    } else {
                                        String timeStamp = String.valueOf(System.currentTimeMillis());
                                        String username = tempAddress.getPhoneNumber();
                                        final String Filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecording_" + username +
                                                "_" + timeStamp + ".3gp";
                                        File DownloadedFile = new File(Filename);
                                        tempAddress.setIsSetAudioAddress(true);
                                        tempAddress.setAudioAddress(Filename);
                                        Download downloaded = S3Util.download(tempAddress.getAudioFileName(), DownloadedFile);
                                        if (downloaded != null) {
                                            contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
                                        } else {
                                            tempAddress.setIsSetAudioAddress(false);
                                            tempAddress.setAudioAddress("");
                                            tempAddress.setAudioFileName("");
                                            contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
                                        }
                                    }
                                } else {
                                    String timeStamp = String.valueOf(System.currentTimeMillis());
                                    String username = tempAddress.getPhoneNumber();
                                    final String Filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecording_" + username +
                                            "_" + timeStamp + ".3gp";
                                    File DownloadedFile = new File(Filename);
                                    tempAddress.setIsSetAudioAddress(true);
                                    tempAddress.setAudioAddress(Filename);
                                    Download downloaded = S3Util.download(tempAddress.getAudioFileName(), DownloadedFile);
                                    if (downloaded != null) {
                                        contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
                                    } else {
                                        tempAddress.setIsSetAudioAddress(false);
                                        tempAddress.setAudioAddress("");
                                        tempAddress.setAudioFileName("");
                                        contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
                                    }
                                }
                            } else {
                                tempAddress.setIsSetAudioAddress(false);
                                tempAddress.setAudioAddress("");
                                tempAddress.setAudioFileName("");
                                contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
                            }
                            k++;
                        }
                        j++;
                    }
                }catch (JSONException jsonException){
                    Log.i("Error","JSONException");
                }
            }
            limit += 20;
        }
        if(i == AllContacts.size()){
            contactsDB.UpdatedOnce(sqLiteDatabase);
            Toast.makeText(this,"Your Contact List Has Been Updated",Toast.LENGTH_SHORT).show();
        }
        contactsDB.CloseDatabase(sqLiteDatabase);
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
