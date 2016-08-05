package com.shopcoup.shareloc;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddAddress extends AppCompatActivity implements OnMapReadyCallback{

    GoogleMap googleMap;
    Address newAddress = null;
    EditText editText2;
    SeekBar seekbar3;
    boolean play;
    int finaltime;
    int reachposition;
    long LastDown = 0;
    boolean textAddressChanged = false;
    boolean MyLocationButtonClicked = false;

    boolean isMapDraggable = false;
    Double newLatitude;
    Double newLongitude;

    private MediaRecorder myAudioRecorder;
    private MediaPlayer mediaPlayer;
    private String addressAudioFile = null;
    private Handler myHandler = new Handler();

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        BaseClass application = (BaseClass) getApplication();
        mTracker = application.getDefaultTracker();

        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                mTracker,
                Thread.getDefaultUncaughtExceptionHandler(),
                AddAddress.this);
        Thread.setDefaultUncaughtExceptionHandler(myHandler);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialise MapFragment and Address
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        googleMap = mapFragment.getMap();
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(AddAddress.this), 1);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.i("Errors","Map Picker error");
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.i("Errors","Map Picker error");
                }
            }
        });
        newAddress = new Address();
        reachposition = 0;

        //Initializing UI elements
        editText2 = (EditText) findViewById(R.id.editText2);

        //Place Picker Intent
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(AddAddress.this), 1);
        } catch (GooglePlayServicesRepairableException e) {
            Log.i("Errors","Map Picker error");
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.i("Errors","Map Picker error");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            newAddress.setVisualAddressLatitude(place.getLatLng().latitude);
            newAddress.setVisualAddressLongitude(place.getLatLng().longitude);

            newLatitude = newAddress.getVisualAddressLatitude();
            newLongitude = newAddress.getVisualAddressLongitude();

            final ImageView ClickMarker = (ImageView) findViewById(R.id.ClickMarker);

            editText2.setText(place.getAddress());

            final Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                            //          .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_36dp))
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .snippet("Click To Edit Location")
                    .draggable(false)
                    .title(place.getName().toString()));

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (isMapDraggable) {
                        Toast.makeText(AddAddress.this, "Location Saved", Toast.LENGTH_LONG).show();
                        isMapDraggable = false;

                        ClickMarker.setVisibility(View.INVISIBLE);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                        marker.setVisible(true);

                        LatLng markerLocation = marker.getPosition();
                        newLatitude = markerLocation.latitude;
                        newLongitude = markerLocation.longitude;
                    } else {
                        Toast.makeText(AddAddress.this, "Click The Marker To Save Location", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AddAddress.this, "Location Saved", Toast.LENGTH_SHORT).show();
                        isMapDraggable = false;

                        ClickMarker.setVisibility(View.INVISIBLE);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                        marker.setVisible(true);

                        LatLng markerLocation = marker.getPosition();
                        newLatitude = markerLocation.latitude;
                        newLongitude = markerLocation.longitude;


                    } else {
                        Toast.makeText(AddAddress.this, "Click The Marker To Save Location", Toast.LENGTH_SHORT).show();
                        isMapDraggable = true;
                        marker.setVisible(false);
                        ClickMarker.setVisibility(View.VISIBLE);
                    }
                }
            });

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    place.getLatLng(), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(place.getLatLng())      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (isMapDraggable) {
                        marker.setPosition(cameraPosition.target);

                        if(!MyLocationButtonClicked)
                            googleMap.setMyLocationEnabled(true);
                        else {
                            googleMap.setMyLocationEnabled(false);
                            MyLocationButtonClicked = false;
                        }
                    }
                }
            });

            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(newAddress.getVisualAddressLatitude(), newAddress.getVisualAddressLongitude()), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(newAddress.getVisualAddressLatitude(), newAddress.getVisualAddressLongitude()))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    googleMap.setMyLocationEnabled(false);
                    MyLocationButtonClicked = true;

                    newLatitude = newAddress.getVisualAddressLatitude();
                    newLatitude = newAddress.getVisualAddressLongitude();
                    return true;
                }
            });

            if(reachposition < 1) {
                //Animating Sliding Layout For Adding Textual Address
                View slidingLayout1 = findViewById(R.id.slidingLayout1);
                slidingLayout1.setVisibility(View.VISIBLE);
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 400.0f, 0.0f);
                translateAnimation.setDuration(800);
                slidingLayout1.startAnimation(translateAnimation);
            }

            reachposition = 1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Add Address Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    //Implemented On clicking save address
    public void onClickAddTextualAddress(View view) {

        //Set Textual Address
        newAddress.setTextualAddress(editText2.getText().toString());

        //Animating Sliding Out For Adding Textual Address
        final View slidingLayout1 = findViewById(R.id.slidingLayout1);
        TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 400.0f);
        translateAnimation.setDuration(800);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                slidingLayout1.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slidingLayout1.startAnimation(translateAnimation);

        //Set Textual Address To Edit Text
        final EditText TextualAddressEditText = (EditText) findViewById(R.id.TextualAddressEditText);
        TextualAddressEditText.setText(newAddress.getTextualAddress());
        TextualAddressEditText.setFocusable(false);
        TextualAddressEditText.setFocusableInTouchMode(false);
        final ImageView TextualAddressEditButton = (ImageView) findViewById(R.id.TextualAddressEditButton);

        //Show Edit Button On Clicking Edit Text and make it editable on clicking edit button
        TextualAddressEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.isFocusable()) {
                    textAddressChanged = true;
                }
                else
                    TextualAddressEditButton.setVisibility(View.VISIBLE);

            }
        });

        TextualAddressEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                newAddress.setTextualAddress(TextualAddressEditText.getText().toString());
                TextualAddressEditText.setFocusable(false);
                TextualAddressEditText.setFocusableInTouchMode(false);
                textAddressChanged = false;
                return true;
            }
        });

        TextualAddressEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextualAddressEditText.isFocusable()) {
                    TextualAddressEditText.setFocusable(true);
                    TextualAddressEditText.setFocusableInTouchMode(true);
                    Toast.makeText(AddAddress.this, "Long Click Address To Save It", Toast.LENGTH_SHORT).show();
                    view.setVisibility(View.GONE);
                }
            }
        });



        View TextualAddressLayout = findViewById(R.id.TextualAddressLayout);
        TextualAddressLayout.setVisibility(View.VISIBLE);
        Button SaveButton = (Button) findViewById(R.id.SaveButton);
        SaveButton.setVisibility(View.VISIBLE);
        ScaleAnimation scaleAnimation =
                new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation =
                new AlphaAnimation(0.0f, 1.0f);
        final AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(400);
        TextualAddressLayout.startAnimation(animationSet);
        SaveButton.startAnimation(animationSet);

        popUpAudioEdit();
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
        reachposition = 2;

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
                if(motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS ||
                   motionEvent.getAction() == MotionEvent.ACTION_DOWN){

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
                            Ringtone ringtone = RingtoneManager.getRingtone(AddAddress.this,Sound);
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

                }else if(motionEvent.getAction() == MotionEvent.ACTION_BUTTON_RELEASE ||
                         motionEvent.getAction() == MotionEvent.ACTION_UP||
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
                                reachposition = 3;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        slidingLayout2.startAnimation(translateAnimation);

                        //Setting Address As Having Audio
                        newAddress.setIsSetAudioAddress(true);
                        newAddress.setAudioAddress(addressAudioFile);
                        newAddress.setAudioAddressChanged(true);

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
                        finaltime = mediaPlayer.getDuration();
                        seekbar3.setMax(mediaPlayer.getDuration());

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.seekTo(0);
                                seekbar3.setProgress(0);
                                pause();
                            }
                        });

                        SharedPreferences sharedPreferences = getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
                        if(!sharedPreferences.getBoolean("EditVisualAddress",false)) {
                            ShowcaseView showcaseView = new ShowcaseView.Builder(AddAddress.this)
                                    .setTarget(new ViewTarget(R.id.mapLayout, AddAddress.this))
                                    .setContentTitle("Edit Marker")
                                    .setContentText("Click on the map to edit marker")
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
                            showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                                @Override
                                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
                                    if(!sharedPreferences.getBoolean("EditTextAddress",false)) {
                                        ShowcaseView showcaseView2 = new ShowcaseView.Builder(AddAddress.this)
                                                .setTarget(new ViewTarget(R.id.TextualAddressEditText, AddAddress.this))
                                                .setContentTitle("Edit Text Address")
                                                .setContentText("Click on the text Address To Edit Text Address")
                                                .hideOnTouchOutside()
                                                .build();
                                        RelativeLayout.LayoutParams ButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                        ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                        ButtonParams.setMargins(convertdptopx(15), 0, 0, convertdptopx(15));
                                        showcaseView2.setButtonPosition(ButtonParams);
                                        showcaseView2.setStyle(R.style.CustomShowcaseTheme);
                                        Button ShowcaseButton = (Button) showcaseView2.findViewById(R.id.showcase_button);
                                        if (Build.VERSION.SDK_INT >= 16)
                                            ShowcaseButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                                        else
                                            ShowcaseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                        showcaseView2.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                                            @Override
                                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                                SharedPreferences sharedPreferences = getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
                                                if(!sharedPreferences.getBoolean("EditAudioAddress",false)) {
                                                    ShowcaseView showcaseView1 = new ShowcaseView.Builder(AddAddress.this)
                                                            .setTarget(new ViewTarget(R.id.recordButtonLayout, AddAddress.this))
                                                            .setContentTitle("Edit Audio Address")
                                                            .setContentText("Click on the edit button to record audio")
                                                            .hideOnTouchOutside()
                                                            .build();
                                                    RelativeLayout.LayoutParams ButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                    ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                                    ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                                    ButtonParams.setMargins(convertdptopx(15), 0, 0, convertdptopx(15));
                                                    showcaseView1.setButtonPosition(ButtonParams);
                                                    showcaseView1.setStyle(R.style.CustomShowcaseTheme);
                                                    Button ShowcaseButton = (Button) showcaseView1.findViewById(R.id.showcase_button);
                                                    if (Build.VERSION.SDK_INT >= 16)
                                                        ShowcaseButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                                                    else
                                                        ShowcaseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                                    showcaseView1.show();
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putBoolean("EditAudioAddress",true);
                                                    editor.apply();
                                                }
                                            }

                                            @Override
                                            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                            }

                                            @Override
                                            public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                            }
                                        });
                                        showcaseView2.show();
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("EditTextAddress",true);
                                        editor.apply();
                                    }
                                }

                                @Override
                                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                }

                                @Override
                                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                }
                            });
                            showcaseView.show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("EditVisualAddress",true);
                            editor.apply();
                        }
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



    //On Clicking Save Address
    public void onClickSaveAddress(View view){
        EditText TextualAddressEditText = (EditText) findViewById(R.id.TextualAddressEditText);
        newAddress.setTextualAddress(TextualAddressEditText.getText().toString());
        final SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
        final Dialog dialog = new Dialog(AddAddress.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_address_dialog);

        final EditText AddressName = (EditText) dialog.findViewById(R.id.AddressName);
        final CheckBox PublicCheckBox = (CheckBox) dialog.findViewById(R.id.PublicCheckBox);
        TextView Cancel = (TextView) dialog.findViewById(R.id.Cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        final TextView Add = (TextView) dialog.findViewById(R.id.Add);
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Address = AddressName.getText().toString();
                if (!AddressName.equals("") && !(AddressName == null)) {
                    if (!sqLiteHandler.isAddressName(Address)) {

                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Completing Adding Address")
                                .build());

                        dialog.dismiss();
                        newAddress.setAddressName(Address);
                        newAddress.setPhoneNumber(sqLiteHandler.getNumber());
                        if(PublicCheckBox.isChecked())
                            newAddress.setIsPublic(true);
                        else
                            newAddress.setIsPublic(false);
                        newAddress.setUID("");
                        newAddress.setAudioFileName("");
                        sqLiteHandler.addAddress(newAddress);
                        Toast.makeText(AddAddress.this,"Address Added",Toast.LENGTH_LONG).show();
                        Intent serviceIntent = new Intent(AddAddress.this,UploadAddressService.class);
                        startService(serviceIntent);
                        Intent intent = new Intent(AddAddress.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        AddressName.setError("Address Name already present");
                    }
                }else{
                    AddressName.setError("Enter an address name");
                }
            }
        });
        dialog.show();
    }


    @Override
    public void onBackPressed() {
        if(reachposition == 3){
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Not Completing Add Address")
                    .build());
            super.onBackPressed();
        }else if(reachposition == 2){
            //Animating Sliding Layout For Adding Textual Address
            final View slidingLayout2 = findViewById(R.id.slidingLayout2);
            reachposition = 3;
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

            SharedPreferences sharedPreferences = getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
            if(!sharedPreferences.getBoolean("EditVisualAddress",false)) {
                ShowcaseView showcaseView = new ShowcaseView.Builder(this)
                        .setTarget(new ViewTarget(R.id.mapLayout, this))
                        .setContentTitle("Edit Marker")
                        .setContentText("Click on the map to edit marker")
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
                showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        SharedPreferences sharedPreferences = getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
                        if(!sharedPreferences.getBoolean("EditTextAddress",false)) {
                            ShowcaseView showcaseView2 = new ShowcaseView.Builder(AddAddress.this)
                                    .setTarget(new ViewTarget(R.id.TextualAddressEditText, AddAddress.this))
                                    .setContentTitle("Edit Text Address")
                                    .setContentText("Click on the text Address To Edit Text Address")
                                    .hideOnTouchOutside()
                                    .build();
                            RelativeLayout.LayoutParams ButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            ButtonParams.setMargins(convertdptopx(15), 0, 0, convertdptopx(15));
                            showcaseView2.setButtonPosition(ButtonParams);
                            showcaseView2.setStyle(R.style.CustomShowcaseTheme);
                            Button ShowcaseButton = (Button) showcaseView2.findViewById(R.id.showcase_button);
                            if (Build.VERSION.SDK_INT >= 16)
                                ShowcaseButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                            else
                                ShowcaseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            showcaseView2.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                                @Override
                                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("ShareLocTutorial",MODE_PRIVATE);
                                    if(!sharedPreferences.getBoolean("EditAudioAddress",false)) {
                                        ShowcaseView showcaseView1 = new ShowcaseView.Builder(AddAddress.this)
                                                .setTarget(new ViewTarget(R.id.recordButtonLayout, AddAddress.this))
                                                .setContentTitle("Edit Audio Address")
                                                .setContentText("Click on the edit button to record audio")
                                                .hideOnTouchOutside()
                                                .build();
                                        RelativeLayout.LayoutParams ButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                        ButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                                        ButtonParams.setMargins(convertdptopx(15), 0, 0, convertdptopx(15));
                                        showcaseView1.setButtonPosition(ButtonParams);
                                        showcaseView1.setStyle(R.style.CustomShowcaseTheme);
                                        Button ShowcaseButton = (Button) showcaseView1.findViewById(R.id.showcase_button);
                                        if (Build.VERSION.SDK_INT >= 16)
                                            ShowcaseButton.setBackground(getResources().getDrawable(R.drawable.button_background));
                                        else
                                            ShowcaseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                        showcaseView1.show();
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("EditAudioAddress",true);
                                        editor.apply();
                                    }
                                }

                                @Override
                                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                }

                                @Override
                                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                }
                            });
                            showcaseView2.show();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("EditTextAddress",true);
                            editor.apply();
                        }
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                });
                showcaseView.show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("EditVisualAddress",true);
                editor.apply();
            }

        }else if(reachposition == 1){
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Not Completing Add Address")
                    .build());
            super.onBackPressed();
        }else{
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Not Completing Add Address")
                    .build());
            super.onBackPressed();
        }
    }

    public int convertdptopx(int dp) {
        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return px;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Not Completing Add Address")
                    .build());
        }
        return super.onOptionsItemSelected(item);
    }
}
