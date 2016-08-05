package com.shopcoup.shareloc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Scanner;
import java.util.regex.Pattern;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    TabLayout tabLayout;
    ProgressDialog progressDialog;
    boolean CompletedDownloading[];
    int TabPosition;

    View SearchBoxContainer;
    AutoCompleteTextView SearchBox;
    ImageButton SearchButton;

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseClass application = (BaseClass) getApplication();
        mTracker = application.getDefaultTracker();

        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                mTracker,
                Thread.getDefaultUncaughtExceptionHandler(),
                MainActivity.this);
        Thread.setDefaultUncaughtExceptionHandler(myHandler);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        boolean ComingViewAddress = getIntent().getBooleanExtra("ViewAddress", false);
        boolean ComingContactAddress = getIntent().getBooleanExtra("ContactAddresses", false);
        if(ComingViewAddress) {
            mViewPager.setCurrentItem(2);
            TabPosition = 2;
        }
        else if (ComingContactAddress) {
            mViewPager.setCurrentItem(0);
            TabPosition = 0;
        }
        else {
            mViewPager.setCurrentItem(1);
            TabPosition = 1;
        }
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //ImageButton SearchBoxMaker = (ImageButton) findViewById(R.id.SearchBoxMaker);
        SearchBox = (AutoCompleteTextView) findViewById(R.id.SearchBox);
        SearchBoxContainer = findViewById(R.id.SearchBoxContainer);
        SearchButton = (ImageButton) findViewById(R.id.SearchButton);

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
        SuggestionAdapter listAdapter = new SuggestionAdapter(this,SimplyList,"");
        SearchBox.setAdapter(listAdapter);

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
                        if(Query.contains("+91-"))
                            Query = Query.replace("+91-","");
                        if(Query.contains("+91"))
                            Query = Query.replace("+91","");
                        if(Query.startsWith("0"))
                            Query = Query.substring(1);
                        search(Query);
                    }
                    else
                        Toast.makeText(MainActivity.this, "Enter A Number", Toast.LENGTH_LONG).show();
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
                if(Query.contains("+91-"))
                    Query = Query.replace("+91-","");
                if(Query.contains("+91"))
                    Query = Query.replace("+91","");
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
                    if(Query.contains("+91-"))
                        Query = Query.replace("+91-","");
                    if(Query.contains("+91"))
                        Query = Query.replace("+91","");
                    if(Query.startsWith("0"))
                        Query = Query.substring(1);
                    search(Query);
                }
                else
                    Toast.makeText(MainActivity.this,"Enter A Number",Toast.LENGTH_LONG).show();
            }
        });

    }

    public void search(final String PhoneNumber){

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Search Contacts")
                .build());

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
                                                Intent intent = new Intent(MainActivity.this, SearchHistory.class);
                                                startActivity(intent);
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
                                    Intent intent = new Intent(MainActivity.this, SearchHistory.class);
                                    startActivity(intent);
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
                        final Dialog dialog = new Dialog(MainActivity.this);
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
                        final Dialog dialog = new Dialog(MainActivity.this);
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
        progressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar_drawable));
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

    @Override
    protected void onPause() {
        if(progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_search){
            if (SearchBoxContainer.getVisibility() == View.GONE) {
                SearchBoxContainer.setVisibility(View.VISIBLE);
                SearchBox.requestFocus();
            }
            else
                SearchBoxContainer.setVisibility(View.GONE);
            SearchBox.setText("");
            SearchButton.setEnabled(false);
        }

        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_refresh){
            Intent intent = new Intent(this,UpdateContactAddress.class);
            startService(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //On Click method for floating button
    public void onClickNewAddress(View view){
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Clicked")
                .setAction("Add Address")
                .build());
        Intent intent = new Intent(MainActivity.this , AddAddress.class);
        startActivity(intent);
    }


    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //To Show Fragment At the desired position
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment = new ContactAddressTab();
                    TabPosition = 0;
                    break;
                case 1:
                    fragment = new MyAddressTab();
                    TabPosition = 1;
                    break;
                case 2:
                    fragment = new RecentAddressTab();
                    TabPosition = 2;
                    break;
            }
            return fragment;
        }

        // Show 2 total pages.
        @Override
        public int getCount() {
            return 3;
        }

        //Show Page Titles
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Contact Addresses";
                case 1:
                    return "My Addresses";
                case 2:
                    return "Recent Addresses";
            }
            return null;
        }


    }

    //Fragment Class For My Address Tab
    public static class MyAddressTab extends Fragment{

        ArrayList<JSONObject> myAddress = null;
        ListView myAddressListView;
        SQLiteHandler sqLiteHandler;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



            //Inflate tab1 layout
            View view = inflater.inflate(R.layout.fragment_tab1, container, false);
            sqLiteHandler = new SQLiteHandler(getContext());
            myAddressListView = (ListView) view.findViewById(R.id.myAddressListView);
            LoadingMyAddressList loadingMyAddressList = new LoadingMyAddressList();
            loadingMyAddressList.execute((Void) null);
            return view;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            SharedPreferences sharedPreferences = getContext().getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
            if(!sharedPreferences.getBoolean("AddAddress",false)) {
                ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity())
                        .setTarget(new ViewTarget(R.id.myFAB, getActivity()))
                        .setContentTitle("Add Address")
                        .setContentText("Click this button to add new Address")
                        .hideOnTouchOutside()
                        .build();
                RelativeLayout.LayoutParams ButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                ButtonParams.setMargins(convertdptopx(15), 0, 0, convertdptopx(15));
                showcaseView.setButtonPosition(ButtonParams);
                showcaseView.setStyle(R.style.CustomShowcaseTheme);
                Button ShowcaseButton = (Button) showcaseView.findViewById(R.id.showcase_button);
                if (Build.VERSION.SDK_INT >= 16)
                    ShowcaseButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                else
                    ShowcaseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                showcaseView.show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("AddAddress",true);
                editor.apply();
            }

        }

        public int convertdptopx(int dp) {
            Resources resources = getResources();
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
            return px;
        }

        public class LoadingMyAddressList extends AsyncTask<Void,Void,Boolean>{
            @Override
            protected void onPostExecute(Boolean success) {
                if(success) {
                    ListAdapter listAdapter = new MyAddressListAdapter(getContext(), myAddress);
                    myAddressListView.setAdapter(listAdapter);
                }
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                myAddress = sqLiteHandler.GetAllAddressNames();
                return true;
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    //Fragment Class For Contact Address Tab
    public static class ContactAddressTab extends Fragment{
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            //Inflate tab2 layout
            View view = inflater.inflate(R.layout.fragment_tab2, container, false);

            //Get All Contacts
            ContactsDB contactsDB = new ContactsDB(getContext());
            ArrayList<Contacts> myContacts = contactsDB.GetAllContacts();
            ListView contactsListView = (ListView) view.findViewById(R.id.contactsListView);
            ListAdapter listAdapter = new ContactAddressListAdapter(getContext(),myContacts);
            contactsListView.setAdapter(listAdapter);
            if(!contactsDB.CheckUpdatedOnce()){
                Intent intent = new Intent(getContext(),UpdateContactAddress.class);
                getContext().startService(intent);
            }
            return view;
        }

    }


    //Fragment Class For Recent Address Tab
    public static class RecentAddressTab extends Fragment{

        ArrayList<JSONObject> RecentAddress = null;
        ListView RecentAddressListView;
        SQLiteHandler sqLiteHandler;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



            //Inflate tab1 layout
            View view = inflater.inflate(R.layout.fragment_tab3, container, false);
            sqLiteHandler = new SQLiteHandler(getContext());
            RecentAddressListView = (ListView) view.findViewById(R.id.RecentAddressListView);
            LoadingRecentAddressList loadingRecentAddressList = new LoadingRecentAddressList();
            loadingRecentAddressList.execute((Void) null);
            return view;
        }

        public class LoadingRecentAddressList extends AsyncTask<Void,Void,Boolean>{
            @Override
            protected void onPostExecute(Boolean success) {
                if(success) {
                    ListAdapter listAdapter = new SearchAddressListAdapter(getContext(), RecentAddress,"MainActivity");
                    RecentAddressListView.setAdapter(listAdapter);
                }
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                RecentAddress = sqLiteHandler.GetAllRecentAddressNames();
                return true;
            }
        }

    }

    public class SuggestionAdapter extends ArrayAdapter<Contacts>{

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
                        ContactsDB contactsDB = new ContactsDB(MainActivity.this);
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
