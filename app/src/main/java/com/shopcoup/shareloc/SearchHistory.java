package com.shopcoup.shareloc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SearchHistory extends AppCompatActivity {

    ProgressDialog progressDialog;
    boolean CompletedDownloading[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
        ArrayList<JSONObject> SearchAddress = sqLiteHandler.GetAllNowAddressNames();

        ListView SearchAddressListView = (ListView) findViewById(R.id.SearchAddressListView);
        SearchAddressListAdapter listAdapter = new SearchAddressListAdapter(this,SearchAddress,"SearchHistory");
        SearchAddressListView.setAdapter(listAdapter);

        ImageButton SearchBoxMaker = (ImageButton) findViewById(R.id.SearchBoxMaker);
        final AutoCompleteTextView SearchBox = (AutoCompleteTextView) findViewById(R.id.SearchBox);
        final View SearchBoxContainer = findViewById(R.id.SearchBoxContainer);
        final ImageButton SearchButton = (ImageButton) findViewById(R.id.SearchButton);

        SearchBoxMaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SearchBoxContainer.getVisibility() == View.GONE) {
                    SearchBoxContainer.setVisibility(View.VISIBLE);
                    SearchBox.requestFocus();
                }
                else
                    SearchBoxContainer.setVisibility(View.GONE);
                SearchBox.setText("");
                SearchButton.setEnabled(false);
            }
        });

        SearchBoxContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    view.setVisibility(View.GONE);
                    SearchBox.setText("");
                    SearchButton.setEnabled(false);
                }
            }
        });

        ArrayList<Contacts> SimplyList = new ArrayList<>();
        SuggestionAdapter listAdapter1 = new SuggestionAdapter(this,SimplyList,"");
        SearchBox.setAdapter(listAdapter1);

        SearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = charSequence.toString();
                if (text.length() == 0) {
                    SearchButton.setEnabled(false);
                    //SearchButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTertiaryText)));
                    return;
                }
                SearchButton.setEnabled(true);
                //SearchButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        SearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action_id, KeyEvent keyEvent) {
                if (action_id == EditorInfo.IME_ACTION_SEARCH) {
                    String Query = SearchBox.getText().toString();
                    if (!Pattern.matches("[a-zA-Z]+", Query)) {
                        if(Query.contains("+91"))
                            Query = Query.replace("+91","");
                        if(Query.contains("+91-"))
                            Query = Query.replace("+91-","");
                        if(Query.startsWith("0"))
                            Query = Query.substring(1);
                        search(Query);
                    }
                    else
                        Toast.makeText(SearchHistory.this, "Enter A Number", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });

        SearchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView Number = (TextView) view.findViewById(R.id.PhoneNumber);
                String Query = Number.getText().toString();
                SearchBox.setText(Query);
                if(Query.contains("+91"))
                    Query = Query.replace("+91","");
                if(Query.contains("+91-"))
                    Query = Query.replace("+91-","");
                if(Query.startsWith("0"))
                    Query = Query.substring(1);
                search(Query);
            }
        });

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Query = SearchBox.getText().toString();
                if(!Pattern.matches("[a-zA-Z]+",Query)) {
                    if(Query.contains("+91"))
                        Query = Query.replace("+91","");
                    if(Query.contains("+91-"))
                        Query = Query.replace("+91-","");
                    if(Query.startsWith("0"))
                        Query = Query.substring(1);
                    search(Query);
                }
                else
                    Toast.makeText(SearchHistory.this,"Enter A Number",Toast.LENGTH_LONG).show();
            }
        });

    }


    public void search(final String PhoneNumber){

        final SQLiteHandler sqLiteHandler = new SQLiteHandler(this);

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
                    String HostURL = getResources().getString(R.string.HostURL) + "getuserlocationpublicdata?mobile=" + PhoneNumber;
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
                                            sqLiteHandler.addRecentAddress(tempAddress);
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
                                                Intent intent = new Intent(SearchHistory.this, SearchHistory.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    }
                                });
                            }else{
                                tempAddress.setIsSetAudioAddress(false);
                                tempAddress.setAudioAddress(null);
                                sqLiteHandler.addRecentAddress(tempAddress);
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
                                    Intent intent = new Intent(SearchHistory.this, SearchHistory.class);
                                    startActivity(intent);
                                    finish();
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
                        final Dialog dialog = new Dialog(SearchHistory.this);
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
                        final Dialog dialog = new Dialog(SearchHistory.this);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_drawable));
        progressDialog.setMessage("Downloading Address");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                AddressDownloader.cancel(true);
            }
        });

        AddressDownloader.execute((Void) null);
    }

    @Override
    protected void onPause() {
        if(progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onPause();
    }

    public class SuggestionAdapter extends ArrayAdapter<Contacts> {

        String highlightText;
        private Filter filter;

        public SuggestionAdapter(Context context, ArrayList<Contacts> objects, String highlightText) {
            super(context, R.layout.searchbox_suggestion_layout, objects);
            this.highlightText = highlightText;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View customView = layoutInflater.inflate(R.layout.searchbox_suggestion_layout, parent, false);

            Contacts tempContact = getItem(position);
            SpannableString ContactName = new SpannableString(tempContact.getName());
            if(tempContact.getName().toLowerCase().contains(highlightText)){
                int firstIndex = tempContact.getName().toLowerCase().indexOf(highlightText);
                int lastIndex = highlightText.length() + firstIndex;
                ContactName.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),firstIndex,lastIndex,0);
            }
            TextView ContactsName = (TextView) customView.findViewById(R.id.ContactName);
            ContactsName.setText(ContactName);

            SpannableString Number = new SpannableString(tempContact.getPhoneNumber());
            if(tempContact.getPhoneNumber().contains(highlightText)){
                int firstIndex = tempContact.getPhoneNumber().indexOf(highlightText);
                int lastIndex = highlightText.length() + firstIndex;
                Number.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),firstIndex,lastIndex,0);
            }
            TextView PhoneNumber = (TextView) customView.findViewById(R.id.PhoneNumber);
            PhoneNumber.setText(Number);

            return  customView;
        }

        @Override
        public Filter getFilter() {
            if(filter == null){
                filter = new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence charSequence) {
                        if(charSequence != null)
                            setHighlightText(charSequence.toString());
                        FilterResults filterResults = new FilterResults();
                        ContactsDB contactsDB = new ContactsDB(SearchHistory.this);
                        if(charSequence != null) {
                            ArrayList<Contacts> SuggestionContacts = contactsDB.GetSuggestions(charSequence.toString());
                            filterResults.values = SuggestionContacts;
                            filterResults.count = SuggestionContacts.size();
                        }
                        return filterResults;
                    }

                    @Override
                    protected void publishResults(final CharSequence charSequence,final FilterResults filterResults) {
                        clear();
                        if(charSequence != null)
                            setHighlightText(charSequence.toString());
                        if(filterResults.count>0){
                            for(Contacts tempContact : (ArrayList<Contacts>) filterResults.values)
                                add(tempContact);
                        }
                    }
                };
            }
            return filter;
        }


        public void setHighlightText(String highlightText) {
            this.highlightText = highlightText;
        }
    }

}
