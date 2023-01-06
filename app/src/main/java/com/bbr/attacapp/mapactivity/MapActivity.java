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
 *   @author: Billy Burgoa Rosso                                              *
 *   Independent Consultant       <billyburgoa@gmail.com>                     *
 *                                                                            *
 ******************************************************************************/
/**
*
* Class controller for activity_map.
* It loads the events from DB to Google Map and it also uses InfoWindowData class
* to instance an object for each event so that this can be casted on CustomWindowAdapter.
*
*
* */
package com.bbr.attacapp.mapactivity;

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
import android.os.Bundle;
import android.os.Looper;
import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bbr.attacapp.eqactivity.EqActivity;
import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqContract;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.mainactivity.MainActivity;
import com.bbr.attacapp.poi.UserPOI;
import com.bbr.attacapp.settings.SettingsActivity;
import com.bbr.attacapp.utilitary.Util;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MapActivity";
    private List<LatLng> positionList;
    private SharedPreferences sharedPref;
    //private Boolean filtData = false;
    private EqDbHelper mEqDbHelper;
    private Util util;
    private boolean isUTC;
    private Menu menu;
    private SupportMapFragment mapFragment;
    private List<Circle> listC1;
    private List<Marker> listM1;
    private List<Circle> listC2;
    private List<Marker> listM2;
    private List<Circle> listC3;
    private List<Marker> listM3;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;
    private Boolean clicked = false;
    private FloatingActionButton fab;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFilter;
    private Boolean clickedFilter;
    //private ProgressDialog mDialog;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double userLat;
    private double userLon;
    private boolean requestingLocationUpdates;
    private Location location;
    private Marker myLocMarker;
    private MarkerOptions myMarkerOptions;
    private ImageButton enableLocationButton;
    private boolean locationEnabled;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    private final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = this;

        //Preferences, DBHelper and Util - instancing
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEqDbHelper = new EqDbHelper(this);
        util = new Util(this);

        rotateOpen = (Animation) AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim);
        rotateClose = (Animation) AnimationUtils.loadAnimation(this,R.anim.rotate_close_anim);
        fromBottom = (Animation) AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim);
        toBottom = (Animation) AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim);

        enableLocationButton = (ImageButton) findViewById(R.id.MapLocationButton);


        isUTC = util.readPrefSwitch(this, "prefUTCSwitch", "default");
        clickedFilter = util.readPrefSwitch(this,"prefFilterSwitch","default");
        util.createVerifierStrings(context);
        /*Reading the data*/
        positionList = new ArrayList<LatLng>();

        //Loading the EQs using an async task

        loadEqs();


        fab = findViewById(R.id.fabList);
        fabFilter = findViewById(R.id.fabfilter);
        fabAdd = findViewById(R.id.fabadd);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !isTaskRoot()){
                    MapActivity.super.onBackPressed();
                    Log.d(TAG,"going back");
                }else{
                    Intent i = new Intent(MapActivity.this, MainActivity.class);
                    startActivity(i);
                    Log.d(TAG,"starting a new list activity");
                }

            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Snackbar.make(view, "Lista de Sismos", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
        });

        fabFilter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Filter or not the data
                clickedFilter = !clickedFilter;


                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("prefFilterSwitch",clickedFilter);
                editor.apply();
                if (clickedFilter){
                    fabFilter.setImageResource(R.drawable.ic_baseline_filter_enable);
                    Snackbar.make(view, "Filtro Habilitado", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else{
                    fabFilter.setImageResource(R.drawable.ic_baseline_filter);
                    Snackbar.make(view, "Filtro Deshabilitado", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }

                if (mMap != null){
                    Log.d(TAG,"Clearing the markers from the map");
                    mMap.clear();
                }else{
                    return;
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = util.createLocationRequest();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestingLocationUpdates = false;
        locationEnabled = sharedPref.getBoolean("locationEnabled", false );
        GPSandNetwork();

        Log.d(TAG, "Network enabled?: "+isNetworkEnabled);

        //if (!isGPSEnabled && !isNetworkEnabled ) {
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
                Log.e(TAG, "Error getting the user location: "+e.toString());
                enableLocationButton.setImageResource(R.mipmap.ic_personlocation_disabled);
                Snackbar.make(findViewById(R.id.map), "Ubicación Deshabilitada", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                locationEnabled = false;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("locationEnabled",false);
                editor.apply();
            }
        }else{
            enableLocationButton.setImageResource(R.mipmap.ic_personlocation_disabled);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("locationEnabled",false);
            editor.apply();
        }

        enableLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationEnabled){
                    //disabling
                    enableLocationButton.setImageResource(R.mipmap.ic_personlocation_disabled);
                    try{
                        myLocMarker.remove();
                    }catch (Exception e){
                        Log.e(TAG,"Not possible to remove line or myLocation Marker");

                    }

                    locationEnabled = false;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("locationEnabled",false);
                    editor.apply();
                    stopLocationUpdates();
                    loadEqs();
                }else{
                    //enableLocationButton.setImageResource(R.mipmap.ic_personlocation_enabled);
                    askPermission();
                    // loadEqs();

                }
            }
        });

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

            if ( clickedFilter ){
                fabFilter.setImageResource(R.drawable.ic_baseline_filter_enable);
            }else{
                fabFilter.setImageResource(R.drawable.ic_baseline_filter);
            }
        }else{
            fab.setVisibility(View.INVISIBLE);
            fabFilter.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(Boolean clicked){
        if ( !clicked ){
            fab.startAnimation(fromBottom);
            fabFilter.startAnimation(fromBottom);
            fabAdd.startAnimation(rotateOpen);
        }else{
            fab.startAnimation(toBottom);
            fabFilter.startAnimation(toBottom);
            fabAdd.startAnimation(rotateClose);
        }

    }

    private void loadEqs() {
        if (mMap != null){
            Log.d(TAG,"Clearing the markers from the map");
            mMap.clear();
        }
        new EqLoadTask().execute();
        //if (positionList.isEmpty() ){
//
        //    Log.d(TAG,"clearing the data on listview");
//
        //    //mEqAdapter = new EqCursorAdapter(this, null);
        //    //listview.setAdapter(mEqAdapter);
        //
        //}else{
        //    Log.d(TAG,"listview is full");
        //}


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setInfoWindowAdapter(new CustomWindowAdapter(this));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setPadding(0,0,800,0);
        zoomControl();
        mMap.setOnCameraMoveListener(() -> {
            int i=0;
            //Circle c;
            boolean visible;
            float cameraPosition = googleMap.getCameraPosition().zoom;
            // Log.d(TAG,"Camera Level: "+cameraPosition);
            if ( listM1 != null){
                for(Marker m:listM1){
                    visible = cameraPosition>=7.1;
                    // Log.d(TAG,"marker Camera moved at level:"+cameraPosition+", VISIBLE:"+visible );
                    m.setVisible(visible);
                    //8 here is your zoom level, you can set it as your need.
                }
                for(Circle c:listC1){
                    visible = cameraPosition>=7.1;
                    //  Log.d(TAG,"Circle Camera moved at level:"+cameraPosition+", VISIBLE:"+visible );
                    c.setVisible(visible);
                    //8 here is your zoom level, you can set it as your need.
                }
            }
            if ( listM2 != null) {
                for(Marker m:listM2){
                    visible = cameraPosition>=6.5;
                    // Log.d(TAG,"marker Camera moved at level:"+cameraPosition+", VISIBLE:"+visible );
                    m.setVisible(visible);
                    //8 here is your zoom level, you can set it as your need.
                }
                for(Circle c:listC2){
                    visible = cameraPosition>=6.5;
                    //  Log.d(TAG,"Circle Camera moved at level:"+cameraPosition+", VISIBLE:"+visible );
                    c.setVisible(visible);
                    //8 here is your zoom level, you can set it as your need.
                }
            }

        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String data = marker.getTitle();
                Log.d(TAG,"Clicked: "+data);
                Intent i = new Intent(MapActivity.this, EqActivity.class);

                i.putExtra("evtid", data);
                startActivity(i);
            }
        });

    }


    private void zoomControl(){

        @SuppressLint("ResourceType") View zoomControls = mapFragment.getView().findViewById(0x1);

        if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params_zoom = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();

            // Align it to - parent top|left
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params_zoom.setMargins(margin, margin, margin, margin);

        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"OnResume...");

        subscribeLocalBroadCast();

        Boolean tmpBool = util.readPrefSwitch(this, "prefUTCSwitch", "default");
        boolean prefChanged = sharedPref.getBoolean("prefChanged",false);
        if ( isUTC != tmpBool){
            isUTC = tmpBool;
        }
        if ( prefChanged ){
            Log.d(TAG,"Preferences Changed. cleaning and loading data again");

            if (mMap != null){
                Log.d(TAG,"Clear the markers from the map");
                mMap.clear();
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("prefChanged",false);
            editor.apply();
        }
        loadEqs();


    }

    @Override
    protected void onPause(){
        super.onPause();
        unSubscribeLocalBroadCast();
        Log.e(TAG,"OnPause...");
        stopLocationUpdates();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        Log.e(TAG,"On Destroy...");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
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

    private class EqLoadTask extends AsyncTask<Void, Void, Cursor[]> {
        //TODO: Change the deprecated ProgessDialog class

        ProgressDialog pd;
        boolean filtData = sharedPref.getBoolean("prefFilterSwitch", false );
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            if ( filtData ){
                pd.setMessage("Cargando datos filtrados. Espere, por favor....");
            }else{
                pd.setMessage("Cargando datos SIN filtro. Espere, por favor....");
            }
            pd.show();
        }
        @Override
        protected Cursor[] doInBackground(Void... voids) {
            Cursor[] cursors = new Cursor[2];

            if ( filtData ){
                cursors[0] = mEqDbHelper.getFilteredEqs();
                cursors[1] = mEqDbHelper.getLastFilteredEq();
            }else{
                cursors[0] = mEqDbHelper.getAllEqs();
                cursors[1] = mEqDbHelper.getLastEq();
            }


            return cursors;
        }

        @SuppressLint("Range")
        @Override
        protected void onPostExecute(Cursor[] cursors) {
            Cursor cursor = cursors[0];
            Cursor cursor1 = cursors[1];
            LatLng newLatLng = null;
            if (cursor != null && cursor.getCount() > 0) {


                //positionList = getAllPositions(cursor1);
                //cursor.moveToFirst();
                Marker m = null;
                MarkerOptions markerOptions = null;
                CustomWindowAdapter customInfoWindow = new CustomWindowAdapter(MapActivity.this, locationEnabled);
                mMap.setInfoWindowAdapter(customInfoWindow);
                listC1 = new ArrayList<>();
                listM1 = new ArrayList<>();
                listC2 = new ArrayList<>();
                listM2 = new ArrayList<>();
                listC3 = new ArrayList<>();
                listM3 = new ArrayList<>();
                float magnitude =0;

                //myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                double tmpUserLat = Double.longBitsToDouble(sharedPref.getLong("userLat", 0));
                double tmpUserLon = Double.longBitsToDouble(sharedPref.getLong("userLon", 0));
                int lastUserLocationTime = sharedPref.getInt("lastUserLocationTime",0);
                long lastUserLocationTimeMs = sharedPref.getLong("lastUserLocationTimeMs",0);

                Log.d(TAG,"Number of events are: "+cursor.getCount());
                for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {

                    String positions = cursor.getString(1);
                    float latitude = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.LAT));
                    float longitude = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.LON));
                    newLatLng = new LatLng(latitude, longitude);

                    String evtid = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.EVTID));

                    magnitude = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.MAGNITUDE));
                    float depth = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.DEPTH));
                    String evtId = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.EVTID));

                    String depthStr = String.format("%.0f",depth)+" km de Profundidad";
                    String magStr = String.format("%.1f",magnitude);
                    String nearplace = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.LOCATION));
                    int nearPlaceDist = -1;
                    try{
                        nearPlaceDist = cursor.getInt(cursor.getColumnIndex(EqContract.EqEntry.DISTNEARLOC));
                    }catch ( Exception e){
                        Log.e(TAG,e.toString());
                    }

                    if(nearPlaceDist == -1){
                        nearPlaceDist = (int) Float.parseFloat(nearplace.split(" ")[0]);
                    }

                    //OriginTime
                    int orTimeUnix = cursor.getInt(cursor.getColumnIndex(EqContract.EqEntry.ORTIME));

                    String dt;

                    if ( isUTC ){
                        dt = util.utcTimestamp2utcISO8601(orTimeUnix);
                        dt = "Fecha y Hora UTC: "+dt;
                    }else{
                        dt = util.utcTimestamp2localISO8601(orTimeUnix);
                        dt = "Fecha y Hora Local: "+dt;
                    }

                    String tmp = util.getTimeSpanFromNow(orTimeUnix);
                    //
                    String timespan = "Ocurrido hace: " + tmp;

                    String agencyStr = cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.AGENCY));
                    agencyStr = agencyStr.toUpperCase();

                    //Marker for containing the infowindow - Not visible
                    markerOptions = new MarkerOptions();

                    //Circle for presenting the epicenter on map
                    CircleOptions circleOptions = new CircleOptions();

                    int timeSpanSeconds = util.getTimespanFromNowSeconds(orTimeUnix);
                    circleOptions.center(newLatLng).radius(magnitude*2000);
                    if ( timeSpanSeconds < 86400 ){
                        circleOptions.fillColor(Color.RED).strokeColor(Color.BLACK).strokeWidth(1);

                    }else if(timeSpanSeconds >= 86400 && timeSpanSeconds< 604800){
                        //Orange with transparence of 70%
                        circleOptions.fillColor(Color.parseColor("#70FFA500")).strokeColor(Color.BLACK).strokeWidth(1);
                    }else if(timeSpanSeconds >= 604800){
                        //Gray with transparence of 700%
                        circleOptions.fillColor(Color.parseColor("#70808080")).strokeColor(Color.BLACK).strokeWidth(1);
                    }else{
                        //
                    }

                    markerOptions.position(newLatLng).alpha(0.0f).anchor(0.5f, 0.5f);


                    InfoWindowData info = new InfoWindowData();
                    info.setMagnitude(magStr);
                    info.setNearplace(nearplace);
                    info.setDepth(depthStr);
                    info.setTimespan(timespan);
                    info.setEpiLat(String.valueOf(latitude));
                    info.setEpiLon(String.valueOf(longitude));
                    info.setNearPlaceDist(String.valueOf(nearPlaceDist));
                    info.setEvtId(evtId);
                    info.setDepthVal(String.valueOf(depth));

                    if( agencyStr.equals("UNA")) {
                        agencyStr = "OVSICORI-UNA";
                    }

                    info.setAgency(agencyStr);
                    info.setDatetime(dt);

                    //Intensity from database that might be reported by the user
                    int intRepTmp = mEqDbHelper.getMaxIntensityReported(evtid);



                    float epiDistance=-1;
                    int intensity = -1;

                    if (intRepTmp <= 0 ){
                        //using estimated intensity instead
                        long tmpNow = (long) util.getUnixTimestampFromTrueTime();
                        long timeDiffMs = 0;
                        if( tmpNow == 0 ){
                            timeDiffMs = util.utcNowTimestampmsecs() - lastUserLocationTimeMs;
                        }else{
                            timeDiffMs = tmpNow - lastUserLocationTimeMs;
                        }
                        int timeDiff = util.utcNowTimestamp() - lastUserLocationTime;

                        if(tmpUserLat != 0 && tmpUserLon != 0 && timeDiffMs < 600000 && locationEnabled){//less than 10 minutes
                            epiDistance = util.distanceTwoPoints(latitude, longitude, tmpUserLat, tmpUserLon);
                            //intensity = util.ipe_allen2012_hyp(epiDistance, magnitude, depth);
                        }else{
                            try{
                                epiDistance = Float.parseFloat(nearplace.split(" ")[0]);
                            }catch (Exception e){
                                Log.e(TAG,e.toString());
                            }
                        }

                        if( epiDistance>0){
                            intensity = util.ipe_allen2012_hyp(epiDistance, magnitude, depth);
                        }
                        info.setIntReportedByUser(false);
                    }
                    if (intRepTmp >0){
                        //There is an user report
                        intensity = intRepTmp;
                        info.setIntReportedByUser(true);
                    }
                    info.setIntensity(String.valueOf(intensity));

                    String intTmp = util.intensity2RomanDescription(intensity);
                    info.setIntensityDescription(intTmp);



                    Circle c = mMap.addCircle(circleOptions);
                    m = mMap.addMarker(markerOptions);
                    m.setTag(info);
                    m.setTitle(evtid);
                    if (magnitude<3.0) {
                        listC1.add(c);
                        listM1.add(m);
                    }else if ( magnitude >=3.0 && magnitude < 4.5){
                        listC2.add(c);
                        listM2.add(m);
                    }else{
                        listC3.add(c);
                        listM3.add(m);
                    }

                }
                // m = mMap.addMarker(markerOptions);
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));

                if ( magnitude < 3 ){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,7.2f));

                }else if ( magnitude >= 3 && magnitude < 4.5 ){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,6.5f));
                }else{
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,6.0f));

                }
                m.showInfoWindow();

            } else {
                // show empty state
                Log.d(TAG,"No Data");
            }
            pd.dismiss();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is
        // present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_about_us)
        {
            util.displayAlertDialogAbout(context,"ATTAC Alerta Temprana de Terremotos", getString(R.string.aboutapp));
            //showAbout();
            return true;
        }


        if ( id == R.id.action_conf)
        {
            Intent i = new Intent(MapActivity.this, SettingsActivity.class);
            startActivity(i);
        }

        if ( id == R.id.action_poi)
        {
            Log.d(TAG,"Starting activity POI");
            Intent i = new Intent(MapActivity.this, UserPOI.class);
            i.putExtra("actName", "MapActivity");
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }



    private void askPermission(){
        //Pemission - Location
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            Log.d(TAG,"Permission for approximated location is granted");

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
                        resolvableApiException.startResolutionForResult(MapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                        Log.e(TAG,"here at 1");
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                        Log.e(TAG,"here at 2");
                    }
                }
            }
        });
    }


    @SuppressLint("MissingPermission")
    private void getLoc(){

        fusedLocationClient.getLastLocation().addOnSuccessListener(MapActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location loc) {

                if (loc !=null){
                    userLat =  loc.getLatitude();
                    userLon = loc.getLongitude();
                    Log.d(TAG,"GetLoc: lat: "+userLat+", Lon: "+userLon);
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

        //Initialize location call back
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
        //First Trying to remove old marker and line

        try{
            myLocMarker.remove();

            Log.d(TAG,"Marker removed");
        }catch (Exception e){
            //not possible to remove
            Log.e(TAG,"Removing marker has an error: "+e.toString());
        }

        //MARKER
        loadEqs();
        //adding the marker if it does not exit or update the location if it does
        Log.d(TAG,"userLat: "+userLat);
        LatLng latLng =new LatLng(userLat, userLon);
        if (mMap == null){
            Log.d(TAG,"Map not ready. Returning");
            return;
        }
        myMarkerOptions = new MarkerOptions();
        myMarkerOptions.position(latLng);
        myMarkerOptions.title("Mi ubicación Aprox.");


        myMarkerOptions.snippet("Lat: "+String.format("%.2f",userLat) + ", Lon: "+String.format("%.2f",userLon));
        myLocMarker = mMap.addMarker(myMarkerOptions);
        myLocMarker.setPosition(latLng);
        myLocMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_personlocation_enabled));

        locationEnabled = true;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("locationEnabled",true);
        editor.apply();
        enableLocationButton.setImageResource(R.mipmap.ic_personlocation_enabled);

        util.setUserLocation(sharedPref, userLat, userLon);

    }


}

