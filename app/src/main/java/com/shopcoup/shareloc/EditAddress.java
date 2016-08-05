package com.shopcoup.shareloc;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URI;

public class EditAddress extends AppCompatActivity implements OnMapReadyCallback{
    boolean addressEdited = false;
    boolean textAddressChanged = true;
    boolean issetaudio = false;
    boolean isMapDraggable = false;

    SeekBar seekbar3;
    boolean play;
    int reachposition = 0;
    long LastDown = 0;
    Address address = null;
    String addressAudioFile = null;
    String newTextAddress = null;
    Double newLatitude;
    Double newLongitude;

    private MediaRecorder myAudioRecorder;
    private MediaPlayer mediaPlayer;
    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String AddressName = bundle.getString("AddressName");
        String UID = bundle.getString("uuid");
        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
        address = sqLiteHandler.getAddressOfName(AddressName,UID);

        //Initialise MapFragment and Address
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final ImageView ClickMarker = (ImageView) findViewById(R.id.ClickMarker);

        GoogleMap googleMap = mapFragment.getMap();
        final LatLng currAddress = new LatLng(address.getVisualAddressLatitude(),address.getVisualAddressLongitude());
        newLatitude = currAddress.latitude;
        newLongitude = currAddress.longitude;
        final Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(currAddress)
                        //          .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_36dp))
                .icon(BitmapDescriptorFactory.defaultMarker())
                .snippet("Click To Edit Location")
                .draggable(false)
                .title(currAddress.toString()));

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (isMapDraggable) {
                    Toast.makeText(EditAddress.this, "Location Saved", Toast.LENGTH_LONG).show();
                    isMapDraggable = false;

                    ClickMarker.setVisibility(View.INVISIBLE);
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    marker.setVisible(true);

                    LatLng markerLocation = marker.getPosition();
                    newLatitude = markerLocation.latitude;
                    newLongitude = markerLocation.longitude;

                    if (newLatitude != address.getVisualAddressLatitude() || newLongitude != address.getVisualAddressLongitude()) {
                        addressEdited = true;
                        View SaveButton = findViewById(R.id.SaveButton);
                        SaveButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(EditAddress.this, "Click The Marker To Save Location", Toast.LENGTH_LONG).show();
                    isMapDraggable = true;
                    marker.setVisible(false);
                    ClickMarker.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isMapDraggable) {
                    Toast.makeText(EditAddress.this, "Location Saved", Toast.LENGTH_SHORT).show();
                    isMapDraggable = false;

                    ClickMarker.setVisibility(View.INVISIBLE);
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    marker.setVisible(true);

                    LatLng markerLocation = marker.getPosition();
                    newLatitude = markerLocation.latitude;
                    newLongitude = markerLocation.longitude;

                    if (newLatitude != address.getVisualAddressLatitude() || newLongitude != address.getVisualAddressLongitude()) {
                        addressEdited = true;
                        View SaveButton = findViewById(R.id.SaveButton);
                        SaveButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(EditAddress.this, "Click The Marker To Save Location", Toast.LENGTH_SHORT).show();
                    isMapDraggable = true;
                    marker.setVisible(false);
                    ClickMarker.setVisibility(View.VISIBLE);
                }
            }
        });

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                currAddress, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currAddress)      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(isMapDraggable) {
                    marker.setPosition(cameraPosition.target);
                }
            }
        });

        //Setting Up Edit Text
        final EditText TextualAddressEditText = (EditText) findViewById(R.id.TextualAddressEditText);
        TextualAddressEditText.setText(address.getTextualAddress());
        TextualAddressEditText.setFocusable(false);
        TextualAddressEditText.setFocusableInTouchMode(false);
        final ImageView TextualAddressEditButton = (ImageView) findViewById(R.id.TextualAddressEditButton);

        //Show Edit Button On Clicking Edit Text and make it editable on clicking edit button
        TextualAddressEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.isFocusable()) {
                    textAddressChanged = true;
                } else
                    TextualAddressEditButton.setVisibility(View.VISIBLE);
            }
        });

        TextualAddressEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                newTextAddress = TextualAddressEditText.getText().toString();
                TextualAddressEditText.setFocusable(false);
                TextualAddressEditText.setFocusableInTouchMode(false);
                textAddressChanged = false;
                return true;
            }
        });

        TextualAddressEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextualAddressEditText.isFocusable()) {
                    TextualAddressEditText.setFocusable(true);
                    TextualAddressEditText.setFocusableInTouchMode(true);

                    Toast.makeText(EditAddress.this, "Long Click Address To Save It", Toast.LENGTH_SHORT).show();
                    view.setVisibility(View.GONE);

                    addressEdited = true;
                    View SaveButton = findViewById(R.id.SaveButton);
                    SaveButton.setVisibility(View.VISIBLE);
                }
            }
        });


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
        }else {
            View playLayout = findViewById(R.id.playLayout1);
            playLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onClickRecord(View view){
        popUpAudioEdit();
    }

    public void popUpAudioEdit(){

        //Animating Sliding Layout For Adding Audio Address
        View slidingLayout2 = findViewById(R.id.slidingLayout2);
        slidingLayout2.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 600.0f, 0.0f);
        translateAnimation.setDuration(1000);
        slidingLayout2.startAnimation(translateAnimation);
        reachposition = 1;

        //Setting Up Path For storing recorded audio file
        String timeStamp = String.valueOf(System.currentTimeMillis());
        SQLiteHandler sqLiteHandler = new SQLiteHandler(getApplicationContext());
        String username = sqLiteHandler.getNumber();
        if(addressAudioFile == null)
            addressAudioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecording_" + username +
                    "_"+timeStamp +  ".3gp";

        //Setting Listener for record button
        final FloatingActionButton recordFAB = (FloatingActionButton) findViewById(R.id.recordFAB);

        //Setting Listener For Audio Recording
        recordFAB.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS ||
                        motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    LastDown = System.currentTimeMillis();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            //Vibrate The Phone
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);

                            //Play Sound
                            Uri Sound = Uri.parse("android.resource://"
                                    + getPackageName() + "/" + R.raw.fadein);
                            Ringtone ringtone = RingtoneManager.getRingtone(EditAddress.this,Sound);
                            ringtone.play();

                            try {

                                //Recording Song and saving to Address Audio File
                                if(myAudioRecorder != null)
                                    myAudioRecorder.release();
                                myAudioRecorder = new MediaRecorder();
                                myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                                myAudioRecorder.setOutputFile(addressAudioFile);
                                myAudioRecorder.prepare();
                                myAudioRecorder.start();

                            } catch (IOException e) {
                                Log.i("Errors", "Error In recording audio");
                            }
                        }
                    };

                    Thread thread = new Thread(runnable);
                    thread.start();


                    //Animation When Creating View
                    ScaleAnimation scaleAnimation =
                            new ScaleAnimation(0.0f,1.0f,0.0f,1.0f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                    AlphaAnimation alphaAnimation =
                            new AlphaAnimation(0.0f,1.0f);
                    final AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(scaleAnimation);
                    animationSet.addAnimation(alphaAnimation);
                    animationSet.setDuration(250);

                    View InvisibleLayout = findViewById(R.id.InvisibleLayout);
                    InvisibleLayout.setVisibility(View.VISIBLE);
                    InvisibleLayout.startAnimation(animationSet);


                } else if (motionEvent.getAction() == MotionEvent.ACTION_BUTTON_RELEASE ||
                        motionEvent.getAction() == MotionEvent.ACTION_UP ||
                        motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                    View InvisibleLayout = findViewById(R.id.InvisibleLayout);
                    InvisibleLayout.setVisibility(View.GONE);

                    long keyPressedDuration = System.currentTimeMillis() - LastDown;
                    if (keyPressedDuration < 1000) {
                        Toast.makeText(getApplicationContext(), "Hold The Mic Button To Record Audio", Toast.LENGTH_LONG).show();
                    } else {

                        //Stoppng The recording
                        myAudioRecorder.stop();
                        myAudioRecorder.release();
                        myAudioRecorder = null;

                        //Animating Sliding Out For Adding Audio Address
                        final View slidingLayout2 = findViewById(R.id.slidingLayout2);
                        TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 600.0f);
                        translateAnimation.setDuration(1000);
                        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                slidingLayout2.setVisibility(View.GONE);
                                reachposition = 0;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        slidingLayout2.startAnimation(translateAnimation);

                        //Setting Address As Having Audio
                        issetaudio = true;
                        addressEdited = true;
                        View SaveButton = findViewById(R.id.SaveButton);
                        SaveButton.setVisibility(View.VISIBLE);

                        //Opening the layout to play the music
                        View playLayout = findViewById(R.id.playLayout);
                        playLayout.setVisibility(View.VISIBLE);
                        ScaleAnimation scaleAnimation =
                                new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        AlphaAnimation alphaAnimation =
                                new AlphaAnimation(0.0f, 1.0f);
                        final AnimationSet animationSet = new AnimationSet(true);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.addAnimation(alphaAnimation);
                        animationSet.setDuration(400);
                        playLayout.startAnimation(animationSet);

                        //Setting Up media player to play audio
                        play = false;
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(addressAudioFile);
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
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
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

    public int convertdptopx(int dp) {
        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return px;
    }

    public void SaveAddress (View view){
        EditText TextualAddressEditText = (EditText) findViewById(R.id.TextualAddressEditText);
        address.setVisualAddressLatitude(newLatitude);
        address.setVisualAddressLongitude(newLongitude);
        address.setTextualAddress(TextualAddressEditText.getText().toString());
        if(issetaudio) {
            address.setIsSetAudioAddress(true);
            address.setAudioAddress(addressAudioFile);
            address.setAudioAddressChanged(true);
        }
        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
        sqLiteHandler.editAddress(address);
        Toast.makeText(EditAddress.this,"Address Edited",Toast.LENGTH_LONG).show();
        Intent serviceIntent = new Intent(EditAddress.this,UploadAddressService.class);
        startService(serviceIntent);
        Intent intent = new Intent(EditAddress.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onBackPressed() {
        if(reachposition == 1){
            //Animating Sliding Layout For Adding Textual Address
            final View slidingLayout2 = findViewById(R.id.slidingLayout2);
            reachposition = 0;
            TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 600.0f);
            translateAnimation.setDuration(1000);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    slidingLayout2.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            slidingLayout2.startAnimation(translateAnimation);
        }else{
            super.onBackPressed();
        }
    }
}
