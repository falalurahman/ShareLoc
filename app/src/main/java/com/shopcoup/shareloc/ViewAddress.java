package com.shopcoup.shareloc;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

public class ViewAddress extends AppCompatActivity implements OnMapReadyCallback {

    SeekBar seekbar3;
    boolean play;
    Address address = null;

    private MediaPlayer mediaPlayer;
    private Handler myHandler = new Handler();

    String CallingActivity;
    SQLiteHandler sqLiteHandler;
    ContactsDB contactsDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_address);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        final String AddressName = bundle.getString("AddressName");
        final String UID = bundle.getString("uuid");
        CallingActivity = bundle.getString("CallingActivity");
        if(CallingActivity != null  && CallingActivity.equals("ContactAddresses")) {
            contactsDB = new ContactsDB(this);
            address = contactsDB.getAddressOfName(AddressName,UID);
        }else {
            sqLiteHandler = new SQLiteHandler(this);
            address = sqLiteHandler.getRecentAddressOfName(AddressName, UID);
        }

        //Initialise MapFragment and Address
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GoogleMap googleMap = mapFragment.getMap();
        final LatLng currAddress = new LatLng(address.getVisualAddressLatitude(),address.getVisualAddressLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(currAddress)
                        //          .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_36dp))
                .icon(BitmapDescriptorFactory.defaultMarker())
                .snippet("Click For Navigation")
                .draggable(false)
                .title(currAddress.toString()));


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + Double.toString(address.getVisualAddressLatitude())
                                + "," + Double.toString(address.getVisualAddressLongitude())));
                startActivity(intent);
            }
        });

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                currAddress, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currAddress)      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        //Setting Up Edit Text
        final TextView TextualAddressEditText = (TextView) findViewById(R.id.TextualAddressEditText);
        TextualAddressEditText.setText(address.getTextualAddress());


        //Setting Up Audio
        if(address.isSetAudioAddress()){
            View playLayout = findViewById(R.id.playLayout);
            playLayout.setVisibility(View.VISIBLE);
            //Setting Up media player to play audio
            play = false;
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(address.getAudioAddress());
            } catch (IOException exception) {
                Log.i("Errors", "Error In Playing");
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Setting up seekbar
            seekbar3 = (SeekBar) findViewById(R.id.seekbar3);
            seekbar3.setMax(mediaPlayer.getDuration());

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(0);
                    seekbar3.setProgress(0);
                    pause();
                }
            });
        }
        if(!CallingActivity.equals("ContactAddresses")) {
            boolean isStored = false;
            isStored = sqLiteHandler.GetRecentAddressStored(UID);
            if (!isStored) {
                ImageButton AddToMyAddressButton = (ImageButton) findViewById(R.id.AddToMyAddressButton);
                AddToMyAddressButton.setVisibility(View.VISIBLE);
                AddToMyAddressButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Address address = sqLiteHandler.getRecentAddressOfName(AddressName, UID);
                        address.setAudioAddressChanged(false);
                        sqLiteHandler.addAddress(address);
                        Intent intent = new Intent(ViewAddress.this, UploadAddressService.class);
                        startService(intent);
                        view.setVisibility(View.GONE);
                    }
                });
            }
        }
    }


    //Implemented on clicking play or pause button
    public void onClickMediaPlay(View view){
        if(play){
            pause();
        }else{
            play();
        }
    }

    //Implemented on clicking play
    public void play(){
        ImageView playButton3 = (ImageView) findViewById(R.id.playButton3);
        playButton3.setImageResource(R.drawable.ic_pause_black_36dp);
        mediaPlayer.start();
        seekbar3.setProgress(mediaPlayer.getCurrentPosition());
        myHandler.postDelayed(UpdateSeekbar, 100);
        play = true;
    }

    //Implemented on clicking pause
    public void pause(){
        ImageView playButton3 = (ImageView) findViewById(R.id.playButton3);
        playButton3.setImageResource(R.drawable.ic_play_arrow_black_36dp);
        mediaPlayer.pause();
        play = false;
    }

    //Thread to Update the seekbar during playing
    private Runnable UpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            seekbar3.setProgress(mediaPlayer.getCurrentPosition());
            if(play)
                myHandler.postDelayed(this,100);
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(CallingActivity.equals("SearchHistory")){
                Intent intent = new Intent(ViewAddress.this,SearchHistory.class);
                startActivity(intent);
                finish();
            }else if(CallingActivity.equals("ContactAddresses")){
                Intent intent = new Intent(ViewAddress.this,ContactAddresses.class);
                intent.putExtra("PhoneNumber",address.getPhoneNumber());
                intent.putExtra("ContactName",contactsDB.GetContactName(address.getPhoneNumber()));
                startActivity(intent);
                finish();
            }
            else{
                Intent intent = new Intent(ViewAddress.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("ViewAddress",true);
                startActivity(intent);
                finish();
            }
        }
        return true;
    }
}
