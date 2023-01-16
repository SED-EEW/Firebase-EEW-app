/******************************************************************************
 *     Copyright (C) by ETHZ/SED                                              *
 *                                                                            *
 *   This program is free software: you can redistribute it and/or modify     *
 *   it under the terms of the GNU Affero General Public License as published *
 *   by the Free Software Foundation, either version 3 of the License, or     *
 *   (at your option) any later version.                                      *
 *                                                                            *
 *   This program is distributed in the hope that it will be useful,          *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            *
 *   GNU Affero General Public License for more details.                      *
 *                                                                            *
 *   -----------------------------------------------------------------------  *
 *                                                                            *
 *   @author: Billy Burgoa Rosso                                               *
 *   Independent Consultant       <billyburgoa@gmail.com>                     *
 *                                                                            *
 ******************************************************************************/
/**
 *
 * This is the first activity and it is the controller for the activity_layout.
 * It loads the events from the DB and uses the EqCursorAdapter to set the events
 * on the scrollview that has the activity_layout.
 * */

package com.bbr.attacapp.mainactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;

import androidx.preference.PreferenceManager;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import androidx.preference.PreferenceManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
//import androidx.core.content.LocalBroadcastManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;
import android.widget.Toast;

import com.bbr.attacapp.eqactivity.EqActivity;
import com.bbr.attacapp.utilitary.EqInfo;
import com.bbr.attacapp.mapactivity.MapActivity;
import com.bbr.attacapp.R;
import com.bbr.attacapp.settings.SettingsActivity;
import com.bbr.attacapp.poi.UserPOI;
import com.bbr.attacapp.dbsqlite.EqContract;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.utilitary.Util;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listview;
    private EqDbHelper mEqDbHelper;
    private TextView mag;
    private TextView depth;
    private TextView location;
    private TextView orTime;
    private TextView recTime;
    private TextView sentTime;
    private TextView timeSpan;
    private TextView agency;
    private TextView tvClock;
    private ImageView imgView;
    private EqCursorAdapter mEqAdapter;
    private Util util;
    private int orTimeUnix;
    private EqInfo eq;
    private SharedPreferences sharedPref;
    private Boolean filtData = false;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private Boolean clicked = false;
    private FloatingActionButton fab;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFilter;
    private FloatingActionButton fabLocation;
    private Context context;
    private LinearLayout lastAlertArea;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double userLat;
    private double userLon;
    private boolean requestingLocationUpdates;
    private boolean locationEnabled;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean firsttime;
    private String lastEvtId;


    private final int REQUEST_CHECK_SETTINGS = 0x1;
    /**/
    private boolean isUTC = false;
    private Menu menu;

    /*days to keep data on SQLite*/
    private int maxdays = 60*24*60*60;

    /**/
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener listener;
    private List<AuthUI.IdpConfig> providers;
    private int AUTH_REQUEST_CODE = 7272;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*registering a broadcaster when a new message has been received*/
        //subscribeLocalBroadCast();

        /*loading the layout*/
        setContentView(R.layout.activity_main);

        util = new Util(this);
        isUTC = util.readPrefSwitch(this, "prefUTCSwitch", "default");
        context = this;

        /*widgets by id*/
        listview = (ListView) findViewById(R.id.ListviewEvents);
        mag = (TextView) findViewById(R.id.EqMagVal);
        depth = (TextView) findViewById(R.id.EqDepthVal);
        location = (TextView) findViewById(R.id.EqLocationVal);
        location = (TextView) findViewById(R.id.EqLocationVal);
        location.setSelected(true);
        orTime = (TextView) findViewById(R.id.ATableOrTimeValue);
        sentTime = (TextView) findViewById(R.id.ATableAgencyVal);
        //recTime = (TextView) findViewById(R.id.ATableRecValue);
        tvClock = (TextView) findViewById(R.id.ATableClockVal);
        timeSpan = (TextView) findViewById(R.id.ATableTimeSpanVal);
        agency = (TextView) findViewById(R.id.ATableAgencyVal);
        imgView = (ImageView) findViewById(R.id.mainLogo);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        filtData = sharedPref.getBoolean("prefFilterSwitch", false );
        rotateOpen = (Animation) AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim);
        rotateClose = (Animation) AnimationUtils.loadAnimation(this,R.anim.rotate_close_anim);
        fromBottom = (Animation) AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim);
        toBottom = (Animation) AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim);
        lastAlertArea = (LinearLayout) findViewById(R.id.lastAlertArea);

        util.createVerifierStrings(context);

        NotificationManagerCompat.from(this).cancelAll();

        fab = findViewById(R.id.fabMap);
        fabFilter = findViewById(R.id.fabfilterList);
        fabAdd = findViewById(R.id.fabaddList);
        fabLocation = findViewById(R.id.fabLocation);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddButtonClicked();
                if ( !isTaskRoot()){
                    MainActivity.super.onBackPressed();
                    Log.d(TAG,"going back");
                }else{
                    Intent i = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(i);
                    Log.d(TAG,"starting a new map activity");
                }

            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, "Mapa de Sismos", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
        });

        fabFilter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Filter or not the data
                filtData = !filtData;


                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("prefFilterSwitch",filtData);
                editor.apply();
                if (filtData){
                    fabFilter.setImageResource(R.drawable.ic_baseline_filter_enable);
                    Snackbar.make(view, "Filtro Habilitado", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else{
                    fabFilter.setImageResource(R.drawable.ic_baseline_filter);
                    Snackbar.make(view, "Filtro Deshabilitado", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }


                loadEqs();

            }
        });


        fabAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Animations!
                onAddButtonClicked();

            }
        });



        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
                new EqTask().execute();
                return;
            }
        });

        /*Removing the DB*/
        //this.deleteDatabase(EqDbHelper.DATABASE_NAME);

        //File dbFile = this.getDatabasePath("eqs.db");
        //if (dbFile.exists()){
        //    this.deleteDatabase(EqDbHelper.DATABASE_NAME);
        //}


        /*
        instancing EQDbHelper
        and removing data from DB up to maxdays ago
        */

        mEqDbHelper = new EqDbHelper(this);
        int now = util.utcNowTimestamp();
        mEqDbHelper.deleteByUnixTimestamp(now - maxdays);

        // loading data from DB
        loadEqs();

        // new Util.ExportDatabaseCSVTask(getApplicationContext()).execute();

        /*
         * Topic Subscriptions!
         */

        //all the alerts are public through this topic.
        util.FCMSubscribeTopic("attacalerts");
        //Topic to notify with EQ events for drills.
        util.FCMSubscribeTopic("attacdrills");

        //Topic for testing alerts
        //util.FCMSubscribeTopic("attactest");
        //personal topic for development purposes
        //util.FCMSubscribeTopic("alerttest");

        /*
         *
         * UNSUBSCRIBE TOPICS!!
         *
         * */

        //Unsubscribe according to your release or usage
        util.FCMUnsubscribeTopic("attactest");
        //personal topic for development purposes
        util.FCMUnsubscribeTopic("alerttest");



        // This is a code which is supposed to keep alive the socket connection between this device
        // and FCM. Not sure if this works
        this.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        this.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));

        /*Clock and elapsed time for last EQ*/
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            String dateString = "";
                            @Override
                            public void run() {

                                if (isUTC){
                                    dateString = util.nowUtcToTimeISO8601();
                                    tvClock.setText("Hora Actual: \n"+dateString+" (UTC)");
                                }else{
                                    dateString = util.nowLocalToTimeISO8601();
                                    tvClock.setText("Hora Actual: \n"+dateString+" (Local)");
                                }

                                if ( orTimeUnix > 0 ){
                                    timeSpan.setText("Hace "+util.getTimeSpanFromNow(orTimeUnix));
                                }


                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = util.createLocationRequest();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestingLocationUpdates = false;
        //Pemission - Location
        locationEnabled = sharedPref.getBoolean("locationEnabled", false );
        firsttime = sharedPref.getBoolean("firstTimeLocation", true);

        // locationEnabled=false;
        GPSandNetwork();

        if(firsttime){
            firsttime = false;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("firstTimeLocation",false);
            editor.apply();
            //askPermission();

        }

        if (locationEnabled ){//the user's pref for location is enabled
            try{
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted!
                    Log.d(TAG,"Permission for approximated location is granted");

                    enableGPS();


                }
            }catch (Exception e)
            {
                Log.e(TAG, "Error getting the user location:"+e.toString());
                fabLocation.setImageResource(R.mipmap.ic_personlocation_disabled);
                Snackbar.make(findViewById(R.id.ListviewEvents), "Ubicación Deshabilitada", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                locationEnabled = false;
            }
        }


        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationEnabled){
                    //disabling
                    fabLocation.setImageResource(R.mipmap.ic_personlocation_disabled);
                    locationEnabled = false;
                    stopLocationUpdates();
                    loadEqs();

                    Snackbar.make(findViewById(R.id.ListviewEvents), "Ubicación Deshabilitada", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("locationEnabled",false);
                    editor.apply();

                }else{
                    askPermission();
                }
            }
        });


        lastAlertArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( lastEvtId != null ){
                    if ( lastEvtId.equals("") ){
                        Log.d(TAG,"last event id is empty ");
                        return;
                    }
                    Log.d(TAG,"Starting eqactivity for event ID: "+ lastEvtId);
                    Intent i = new Intent(MainActivity.this, EqActivity.class);

                    i.putExtra("evtid", lastEvtId );
                    startActivity(i);
                }else{
                    Log.d(TAG,"No last event yet");
                }

            }
        });

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser == null){
            init();
        }else{
            try{
                Log.d(TAG,"USER: "+currentUser.getDisplayName());
            }catch (Exception e){
                Log.e( TAG,"User error(?): "+ e.toString() );
            }

        }


        //starting the service without checking any permission
        //We want to have a foreground service so that
        //the Android OS does not put the app
        //on restriction zone.
        //For Android 13 and ahead it will be necessary
        //to have the BACKGROUND LOCATION PERMISSION
        //to avoid being restricted.
        util.starServiceFunc(this);


    }//end of onCreate method

    //init function for asking registering with email or as an invited
    private void init(){
        providers = Arrays.asList(
                new AuthUI.IdpConfig.AnonymousBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.attac_logo)
                .setIsSmartLockEnabled(false)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG,"RESULT: "+result.toString());
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            try{
                Log.e(TAG,"ERROR WHILE SIGNING IN: "+ response.getError().getErrorCode());
            }catch (Exception e){
                Log.e(TAG,"ERROR in response: "+e.toString());
            }

        }
    }


    private void onAddButtonClicked(){
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    @SuppressLint("RestrictedApi")
    private void setVisibility(Boolean clicked){
        if ( !clicked ){
            fab.setVisibility(View.VISIBLE);
            fabFilter.setVisibility(View.VISIBLE);
            fabLocation.setVisibility(View.VISIBLE);


        }else{
            fab.setVisibility(View.INVISIBLE);
            fabFilter.setVisibility(View.INVISIBLE);
            fabLocation.setVisibility(View.INVISIBLE);

        }
        if ( filtData ){
            fabFilter.setImageResource(R.drawable.ic_baseline_filter_enable);
        }else{
            fabFilter.setImageResource(R.drawable.ic_baseline_filter);
        }
    }

    private void setAnimation(Boolean clicked){
        if ( !clicked ){
            fab.startAnimation(fromBottom);
            fabFilter.startAnimation(fromBottom);
            fabLocation.startAnimation(fromBottom);
            fabAdd.startAnimation(rotateOpen);
        }else{
            fab.startAnimation(toBottom);
            fabFilter.startAnimation(toBottom);
            fabLocation.startAnimation(toBottom);
            fabAdd.startAnimation(rotateClose);
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG,"OnStart...");

    }

    @Override
    protected void onStop(){
        super.onStop();
        if(listener != null){
            mAuth.removeAuthStateListener(listener);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"OnResume...");

        filtData = sharedPref.getBoolean("prefFilterSwitch", false );
        subscribeLocalBroadCast();
        Boolean tmpBool = util.readPrefSwitch(this, "prefUTCSwitch", "default");
        boolean changed = sharedPref.getBoolean("prefChanged",false);
        if ( isUTC != tmpBool){
            isUTC = tmpBool;

        }

        if ( changed ){
            Log.d(TAG, "preferences changed. Loading data");
            loadEqs();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("prefChanged",false);
            editor.apply();
        }

        Log.d(TAG,"TrueTime: " + util.getUnixTimestampFromTrueTime());
        Log.d(TAG,"UTC Time on this device: " + util.utcNowTimestampmsecs() );


        //getting the last push notification on the sharedprefs
        String lastMsg = sharedPref.getString("lastMsg", "NOT_EVENT");
        Log.d(TAG,"LASTMSG: "+lastMsg);
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG,"OnRestart...");
        loadEqs();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unSubscribeLocalBroadCast();
        Log.e(TAG,"OnPause...");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e(TAG,"OnDestroy...");
    }

    private void subscribeLocalBroadCast(){
        try{
            LocalBroadcastManager.getInstance(this).registerReceiver(mHandler,new IntentFilter("com.bbr.attacapp_FCM-MESSAGE"));
        }catch (Exception e){
            Log.e(TAG,"Error while registering to local broadcaster");
            Log.e(TAG,"Msg: "+e.toString() );
        }
        return;
    }

    private void unSubscribeLocalBroadCast(){
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandler);
        }catch (Exception e){
            Log.e(TAG,"Error while unregistering to local broadcaster");
            Log.e(TAG,"Msg: "+e.toString() );
        }

    }
    private void loadEqs() {
        if (listview.getCount() > 0){
            listview.setAdapter(null);
            Log.d(TAG,"clear the data on listview");
        }else{
            Log.d(TAG,"listview is empty");
        }

        mEqAdapter = new EqCursorAdapter(this, null, locationEnabled);
        listview.setAdapter(mEqAdapter);
        new EqLoadTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about_us)
        {
            util.displayAlertDialogAbout(context,"ATTAC Alerta Temprana de Terremotos", getString(R.string.aboutapp));
            //showAbout();
            return true;
        }

        if ( id == R.id.action_conf)
        {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        }
        if ( id == R.id.action_poi)
        {
            Intent i = new Intent(MainActivity.this, UserPOI.class);
            i.putExtra("actName", "MainActivity");
            startActivity(i);
        }



        return super.onOptionsItemSelected(item);
    }

    private void startEqActivity(){
        Intent i = new Intent(MainActivity.this, EqActivity.class);

        i.putExtra("evtid", eq.getEvtId());
        startActivity(i);
    }

    /*Handler from FCM service*/
    private BroadcastReceiver mHandler  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            if ( status.equals("new") ) {
                Toast.makeText(context,"Nueva Alerta Recibida",Toast.LENGTH_SHORT).show();
            }
            loadEqs();
        }
    };

    private class EqLoadTask extends AsyncTask<Void, Void, Cursor[]> {

        //TODO: Progress dialog is deprecated. It must be changed
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            if ( filtData ){
                pd.setMessage("Cargando datos filtrados. Espere, por favor....");
            }else{
                pd.setMessage("Cargando datos SIN filtro. Espere, por favor....");
            }
            try{
                pd.show();
            }catch (Exception e){
                Log.e(TAG,"Error showing the dialog: "+e.toString());
            }

        }
        @Override
        protected Cursor[] doInBackground(Void... voids) {
            Cursor[] cursors = new Cursor[2];
            filtData = sharedPref.getBoolean("prefFilterSwitch", false );
            if ( filtData ){
                Log.d(TAG,"Getting filtered data from DB");
                cursors[0] = mEqDbHelper.getFilteredEqs();
                cursors[1] = mEqDbHelper.getLastFilteredEq();
            }else{
                Log.d(TAG,"Getting unfiltered data from DB");
                cursors[0] = mEqDbHelper.getAllEqs();
                cursors[1] = mEqDbHelper.getLastEq();
            }


            return cursors;
        }

        @SuppressLint("Range")
        @Override
        protected void onPostExecute(Cursor[] cursors) {
            Cursor cursor1 = cursors[0];
            Cursor cursor = cursors[1];
            if (cursors[0] != null && cursors[0].getCount() > 0) {
                mEqAdapter.swapCursor(cursor1);

                /* Filling the last eq info */
                Log.d(TAG,"Filling the last eq info...");
                while(cursor.moveToNext()){
                    //Magnitude
                    @SuppressLint("Range") float magnitude = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.MAGNITUDE));
                    mag.setText(String.valueOf(magnitude));
                    if ( magnitude < 4.5 ){
                        mag.setTextColor(Color.parseColor("#D2D2D2"));
                    }else if( magnitude >= 4.5 && magnitude < 5.8 ){
                        mag.setTextColor(Color.parseColor("#FFA500"));
                    }else{
                        mag.setTextColor(Color.RED);
                    }

                    //Depth
                    @SuppressLint("Range") float depthVal = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.DEPTH));
                    depth.setText(String.format("%.0f",depthVal)+" km");
                    String dt = "YYYY-MM-dd HH:mm:ss";
                    //OriginTime
                    orTimeUnix = cursor.getInt(cursor.getColumnIndex(EqContract.EqEntry.ORTIME));
                    if (isUTC){
                        dt = util.utcTimestamp2utcISO8601(orTimeUnix);
                        orTime.setText( dt + " (UTC)");
                    }else{
                        dt = util.utcTimestamp2localISO8601(orTimeUnix);
                        orTime.setText( dt + " (Local)");
                    }


                    //Agency
                    String a = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.AGENCY)).toUpperCase();
                    if( a.equals("UNA")) {
                        a = "OVSICORI-UNA";
                        imgView.setImageResource(R.mipmap.ic_ovsicori);
                    }
                    if( a.equals("INETER")) {
                        //a = "OVSICORI-UNA";
                        imgView.setImageResource(R.mipmap.ic_ineter);
                    }
                    if( a.equals("MARN")) {
                        imgView.setImageResource(R.mipmap.ic_marn);
                    }
                    if( a.equals("INSIVUMEH")) {
                        imgView.setImageResource(R.mipmap.ic_insivumeh);
                    }
                    agency.setText(a);
                    //logo

                    // location
                    String locationVal = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.LOCATION));
                    location.setText(locationVal);

                    //last event ID
                    lastEvtId = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.EVTID));
                    Log.d(TAG, "setting the new last evt ID : "+ lastEvtId);

                    /*breaking here because we want to get the last event*/
                    break;
                }

            } else {
                // show empty state
                Log.e(TAG,"Last event does not exist");
            }
            pd.dismiss();
        }
    }

    private class EqTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            Cursor cursor = mEqDbHelper.getAllEqs();
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {

                Cursor eqCursor = (Cursor) mEqAdapter.getCursor();

                eq = new EqInfo(eqCursor);

                startEqActivity();

                return;
            }
        }
    }

    private void askPermission(){
        //Permission - Location
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            Log.d(TAG,"Permission for approximated location is granted");
            //task = util.GPSenabled(context);
            enableGPS();


        }
    }


    private void GPSandNetwork(){
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void enableGPS(){
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());



        task.addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG,"Location settings (GPS) is ON.");
                //Get location
                getCurrentGPSLocation();
            }
        });

        task.addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"Location settings (GPS) is OFF.");

                if (e instanceof ResolvableApiException){
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void getLoc(){

        fusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location loc) {

                if (loc !=null){
                    Log.d(TAG,"Location obtained");
                    userLat =  loc.getLatitude();
                    userLon = loc.getLongitude();
                    Log.d(TAG,"GetLoc Method: lat: "+userLat+", Lon: "+userLon);
                    setUserLocation();

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG,"RQ: "+requestCode+", RC: "+resultCode );
        switch (requestCode)
        {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        // User enabled the GPS
                        Log.d(TAG,"USer enabled the GPS");
                        getCurrentGPSLocation();

                        break;
                    case Activity.RESULT_CANCELED:
                        // User didn't accept to set GPS on
                        Log.d(TAG,"USer DIDN'T enable the GPS");
                        locationEnabled = false;
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("locationEnabled",false);
                        editor.apply();
                        Toast.makeText(this, "Ubicación NO disponible", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentGPSLocation() {

        getLoc();
        Log.d(TAG, "Request done");

        locationCallback = util.createLocationCallback(TAG);

        //Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(!requestingLocationUpdates){
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
            requestingLocationUpdates = true;
        }

    }

    private void stopLocationUpdates() {
        if(fusedLocationClient != null){
            try{
                fusedLocationClient.removeLocationUpdates(locationCallback);
                requestingLocationUpdates = false;
                Log.d(TAG,"Removed Callback...");
            }catch (Exception e){
                Log.e(TAG,"Error removing the location updates: "+e.toString());
            }

        }

    }

    private void setUserLocation(){
        //just storing in MyPreferences
        util.setUserLocation(sharedPref, userLat, userLon);

        locationEnabled = true;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("locationEnabled",false);
        editor.apply();

        loadEqs();

        fabLocation.setImageResource(R.mipmap.ic_personlocation_enabled);

        Snackbar.make(findViewById(R.id.ListviewEvents), "Ubicación Habilitada", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        SharedPreferences.Editor editor = sharedPref.edit();
        if( requestCode == 1 && grantResults[0] >= 0 ) {
            Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
            enableGPS();
        }else{
            if(requestCode == 1 ){
                locationEnabled = false;
                //SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("locationEnabled",false);
                editor.apply();

                Log.d(TAG,"Not granted for location");
            }

        }

    }


}
