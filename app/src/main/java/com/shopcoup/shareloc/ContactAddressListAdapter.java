package com.shopcoup.shareloc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.Download;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class ContactAddressListAdapter extends ArrayAdapter<Contacts>{

    ProgressDialog progressDialog;
    boolean CompletedDownloading[];

    public ContactAddressListAdapter(Context context, List<Contacts> objects) {
        super(context, R.layout.row_contact_list_found, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflating Layout Of List
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.row_contact_list_found, parent, false);

        //Get A Contact
        final Contacts tempContact = getItem(position);

        //Set Contact Name
        TextView contactView = (TextView) customView.findViewById(R.id.contactView);
        contactView.setText(tempContact.getName());

        //Check If Contact Number Has Address Stored
        final String PhoneNumber = tempContact.getPhoneNumber();

        final boolean addressPresent = tempContact.isHasAddress();

        ImageView button = (ImageView) customView.findViewById(R.id.button7);
        if(addressPresent){
            button.setImageResource(R.drawable.viewaddress_icon1);
        }
        else{
            button.setImageResource(R.drawable.askaddress_icon1);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addressPresent){
                    Intent intent = new Intent(getContext(),ContactAddresses.class);
                    intent.putExtra("ContactName",tempContact.getName());
                    intent.putExtra("PhoneNumber",PhoneNumber);
                    getContext().startActivity(intent);
                }else {
                    search(tempContact.getName(),PhoneNumber);
                }
            }
        });
        return customView;
    }

    public void search(final String ContactName,final String PhoneNumber){

        final ContactsDB contactsDB = new ContactsDB(getContext());
        final SQLiteDatabase sqLiteDatabase = contactsDB.GetDatabase();

        final AsyncTask<Void,Void,Boolean> AddressDownloader = new AsyncTask<Void,Void,Boolean>() {
            boolean isNoAddress = false;
            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                String output="";
                try {
                    String HostURL = getContext().getResources().getString(R.string.HostURL) + "getuserlocationpublicdata?mobile=" + PhoneNumber;
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
                            isNoAddress = true;
                            return false;
                        }
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
                                            contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
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
                                                contactsDB.CloseDatabase(sqLiteDatabase);
                                                contactsDB.Upgrade((Activity)getContext());
                                                Intent intent = new Intent(getContext(), ContactAddresses.class);
                                                intent.putExtra("ContactName",ContactName);
                                                intent.putExtra("PhoneNumber",PhoneNumber);
                                                getContext().startActivity(intent);
                                            }
                                        }
                                    }
                                });
                            }else{
                                tempAddress.setIsSetAudioAddress(false);
                                tempAddress.setAudioAddress(null);
                                contactsDB.UpdateContactAddress(tempAddress,sqLiteDatabase);
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
                                    contactsDB.CloseDatabase(sqLiteDatabase);
                                    contactsDB.Upgrade((Activity)getContext());
                                    Intent intent = new Intent(getContext(), ContactAddresses.class);
                                    intent.putExtra("ContactName", ContactName);
                                    intent.putExtra("PhoneNumber",PhoneNumber);
                                    getContext().startActivity(intent);
                                }
                            }
                        }
                    }catch (JSONException jsonException){
                        return false;
                    }
                }else{
                    isNoAddress = true;
                    return false;
                }
                return true;
            }


            @Override
            protected void onPostExecute(Boolean success) {
                if(!success){
                    if(isNoAddress){
                        progressDialog.dismiss();
                        progressDialog = null;
                        final Dialog dialog = new Dialog(getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.alert_dialog);

                        TextView DialogTitle = (TextView) dialog.findViewById(R.id.DialogTitle);
                        TextView DialogMessage = (TextView) dialog.findViewById(R.id.DialogMessage);
                        TextView Ok = (TextView) dialog.findViewById(R.id.Ok);
                        DialogTitle.setText("Sorry");
                        DialogMessage.setText("No Address Found");
                        Ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                    }else{
                        progressDialog.dismiss();
                        progressDialog = null;
                        final Dialog dialog = new Dialog(getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.alert_dialog);

                        TextView DialogTitle = (TextView) dialog.findViewById(R.id.DialogTitle);
                        TextView DialogMessage = (TextView) dialog.findViewById(R.id.DialogMessage);
                        TextView Ok = (TextView) dialog.findViewById(R.id.Ok);
                        DialogTitle.setText("Sorry");
                        DialogMessage.setText("Error in Downloading Address");
                        Ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                    }
                }
            }
        };

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.progressbar_drawable));
        progressDialog.setMessage("Downloading Address...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                AddressDownloader.cancel(true);
            }
        });

        AddressDownloader.execute((Void) null);
    }
}
