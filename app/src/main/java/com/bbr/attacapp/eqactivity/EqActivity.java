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
* Class for EQ information which is a mid-term
* between controller and model as a software architectural
*
 * */
package com.bbr.attacapp.eqactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.LocalBroadcastManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import android.support.v7.app.AppCompatActivity;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bbr.attacapp.utilitary.EqInfo;
import com.bbr.attacapp.equpdatesactivity.EqUpdatesActivity;
import com.bbr.attacapp.intensityreportactivity.IntensityReport;
import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.utilitary.Util;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;


public class EqActivity  extends AppCompatActivity implements OnMapReadyCallback {
    private Util util;
    private Boolean isUTC;
    private Context context;
    private static final String TAG = "EqActivity";
    private String evtid ;
    private EqDbHelper eqDbHelper;
    private EqInfo eq;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private TableRow tr ;
    private TextView updatesButton;
    private Button intensityRepButton;
    private Button reportBtn;
    private TextView intensityTextStatus;
    private TextView intensityDescription;
    private LocationManager locationManager;
    private Location location;
    private Marker myLocMarker;
    private double userLat = -999.0;
    private double userLon = -999.0;
    private MarkerOptions myMarkerOptions;
    private ImageButton enableLocationButton;
    private boolean locationEnabled;
    private Polyline line;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    Task<LocationSettingsResponse> task;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final int NUM_UPDATES = 8;
    LocationRequest locationRequest;
    private boolean requestingLocationUpdates;

    private final int REQUEST_CHECK_SETTINGS = 0x1;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*loading the layout*/
        setContentView(R.layout.eq_layout);
        util = new Util(this);
        eqDbHelper = new EqDbHelper(this);
        isUTC = util.readPrefSwitch(this, "prefUTCSwitch", "default");
        context = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(getIntent().getExtras() != null){
            evtid = getIntent().getStringExtra("evtid");
            Log.d(TAG," evtid => " + evtid);
        }
        else{
            //event does not exist!
            finish();
        }


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.eqmap);
        mapFragment.getMapAsync(this);


        updatesButton = (TextView) findViewById(R.id.textButtonUpdates);
        intensityRepButton = (Button) findViewById(R.id.EqButtonIntRep);
        intensityTextStatus = (TextView) findViewById(R.id.EqIntensityText);
        intensityDescription = (TextView) findViewById(R.id.EqIntensityVal);
        enableLocationButton = (ImageButton) findViewById(R.id.EqButtonLocation);

        tr = (TableRow) findViewById(R.id.EqUpdatesRow);

        updatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here it is open the EqUpdates display on the app
                Intent i = new Intent(EqActivity.this, EqUpdatesActivity.class);

                i.putExtra("evtid", evtid);
                startActivity(i);
            }
        });
        intensityRepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intensity Report View
                Intent i = new Intent(EqActivity.this, IntensityReport.class);

                i.putExtra("evtid", evtid);
                startActivity(i);
            }
        });



        locationRequest = util.createLocationRequest();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        requestingLocationUpdates = false;
        locationEnabled = sharedPref.getBoolean("locationEnabled", false );
        GPSandNetwork();

        Log.d(TAG, "Network enabled?: "+isNetworkEnabled);

        if ( locationEnabled ){//the user's pref for location is enabled
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
                        line.remove();
                        myLocMarker.remove();
                    }catch (Exception e){
                        Log.e(TAG,"Not possible to remove line or myLocation Marker. See below...");
                        Log.e(TAG,e.toString());
                    }

                    locationEnabled = false;

                    loadEq();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("locationEnabled",false);
                    editor.apply();
                    stopLocationUpdates();

                }else{

                    askPermission();

                }
            }
        });


    }

    private void GPSandNetwork(){
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void askPermission(){
        //Pemission - Location
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG,"Permission for approximated location is granted");

            enableGPS();

        }
    }

    private void loadEq(){
        Cursor evtCursor =eqDbHelper.getEqById(evtid);
        if(evtCursor.getCount() >= 1) {
            evtCursor.moveToNext();
        }else{
            Toast.makeText(context,"El evento ya NO existe.",Toast.LENGTH_SHORT).show();

            finish();
            return;
        }
        try{
            eq = new EqInfo(evtCursor);
        }catch (Exception e){
            Log.e(TAG,"Event with ID:"+evtid+" does not exist. Leaving");
            Toast.makeText(context,"El evento ya NO existe.",Toast.LENGTH_SHORT).show();
            finish();
            return;

        }


        Cursor c = eqDbHelper.getEqAllById(evtid);
        int updates = c.getCount();
        //if( updates == 1){
        //    tr.setVisibility(View.GONE);
        //}else{
        updatesButton.setText((updates-1)+" (click para más info.)");

        TextView dMagVal = (TextView) findViewById(R.id.EqActMagVal);
        TextView DOrTime = (TextView) findViewById(R.id.EqDateTimeValue);
        TextView timeSpan = (TextView) findViewById(R.id.EqTimeSpanValue);
        TextView nearPlace = (TextView) findViewById(R.id.EqLocationVal);
        TextView evtId = (TextView) findViewById(R.id.EqEvtIdValue);
        TextView lat = (TextView) findViewById(R.id.EqLatValue);
        TextView lon = (TextView) findViewById(R.id.EqLonValue);
        TextView depth = (TextView) findViewById(R.id.EqActDepthVal);
        TextView dAgency = (TextView) findViewById(R.id.EqAgencyValue);
        TextView dType = (TextView) findViewById(R.id.EqMsgTypeValue);
        TextView dStatus = (TextView) findViewById(R.id.EqStatusValue);
        TextView delay = (TextView) findViewById(R.id.EqDelayValue);
        ImageView eqLogoView = (ImageView) findViewById(R.id.EqImgLogo);

        float magnitude = eq.getMagnitude();

        if ( magnitude < 4.5 ){
            dMagVal.setTextColor(Color.parseColor("#D2D2D2"));
        }else if( magnitude >= 4.5 && magnitude < 5.8 ){
            dMagVal.setTextColor(Color.parseColor("#FFA500"));
        }else{
            dMagVal.setTextColor(Color.RED);
        }

        dMagVal.setText(""+eq.getMagnitude());
        depth.setText(String.format("%.0f",eq.getDepth())+" km");
        nearPlace.setText(eq.getLocation());

        lat.setText(String.valueOf(eq.getLat()));
        lon.setText(String.valueOf(eq.getLon()));

        if (isUTC){
            /*changing to local time*/
            DOrTime.setText(util.utcTimestamp2utcISO8601(eq.getOrTime())+" (UTC)");

        }
        else{
            /*changing to UTC time*/
            DOrTime.setText(util.utcTimestamp2localISO8601(eq.getOrTime())+" (Local)");
        }

        String agency = eq.getAgency().toUpperCase();
        if( agency.equals("UNA")) {
            agency = "OVSICORI-UNA";
            eqLogoView.setImageResource(R.mipmap.ic_ovsicori);
        }

        if( agency.equals("INETER")) {
            //a = "OVSICORI-UNA";
            eqLogoView.setImageResource(R.mipmap.ic_ineter);
        }

        if( agency.equals("MARN")) {
            eqLogoView.setImageResource(R.mipmap.ic_marn);
        }

        if( agency.equals("INSIVUMEH")) {
            eqLogoView.setImageResource(R.mipmap.ic_insivumeh);
        }

        dAgency.setText(agency);

        int uTimeStamp = eq.getOrTime();
        timeSpan.setText("Ocurrido hace "+util.getTimeSpanFromNow(uTimeStamp));

        String strStatus = eq.getStatus();
        String strType = eq.getType();

        if ( strStatus.equals("automatic")){
            dStatus.setText("Automático");
            dStatus.setTextColor(Color.RED);
        }else if ( strStatus.equals("manual")){
            dStatus.setText("Revisado");
            dStatus.setTextColor(Color.parseColor("#024b30"));

        }else{
            dStatus.setText("No Existe");
            dStatus.setTextColor(Color.DKGRAY);
        }

        if ( strType.equals("alert") ){
            dType.setText("Alerta");
            dType.setTextColor(Color.RED);
        }else{
            dType.setText("Catálogo");
            dType.setTextColor(Color.parseColor("#024b30"));
        }

        evtId.setText(eq.getEvtId());


        delay.setText((eq.getRecTime()-eq.getSentTime())/1000.+" s");


        //Intensity
        setIntensity();

        if( !requestingLocationUpdates ){
            getCurrentGPSLocation();
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG,"OnStart...");
        if ( locationEnabled ){

            enableLocationButton.setImageResource(R.mipmap.ic_personlocation_enabled);
        }else{

            enableLocationButton.setImageResource(R.mipmap.ic_personlocation_disabled);
        }

    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"OnResume...");
        subscribeLocalBroadCast();

        loadEq();

    }

    @Override
    protected void onPause(){
        super.onPause();
        unSubscribeLocalBroadCast();
        Log.e(TAG,"OnPause...");
        stopLocationUpdates();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Map controllers - enabling...
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng newLatLng = new LatLng(eq.getLat(), eq.getLon());
        Marker m = null;
        MarkerOptions markerOptions = new MarkerOptions();
        CircleOptions circleOptions = new CircleOptions();

        circleOptions.center(newLatLng).radius(eq.getMagnitude()*2000);
        circleOptions.fillColor(Color.RED).strokeColor(Color.BLACK).strokeWidth(1);
        Circle c = mMap.addCircle(circleOptions);


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,7.2f));

        if (userLon != -999.0 && userLat != -999.0 ){
            Log.d(TAG,"Location already Available");

        }

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

    /*Handler from FCM service*/
    private BroadcastReceiver mHandler  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            if ( status.equals("new") ) {
                Toast.makeText(context,"Nueva Alerta Recibida",Toast.LENGTH_SHORT).show();
            }
            loadEq();
        }
    };



    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        if( requestCode == 1 && grantResults[0] >= 0 ) {
            Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
            enableGPS();
        }else{
            locationEnabled = false;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("locationEnabled",false);
            editor.apply();

            Log.d(TAG,"Not granted for location");
        }
    }

    private void setIntensity(){
        int nearPlaceDist = -1;
        try{
            nearPlaceDist = eq.getNearPlaceDist();
        }catch (Exception e){

            nearPlaceDist = (int) Float.parseFloat(eq.getLocation().split(" ")[0]);

        }

        List<Object> arrIntensity = util.getIntensityDescAndColor(sharedPref,eq.getLat(), eq.getLon(), eq.getDepth(),
                eq.getMagnitude(), nearPlaceDist, evtid, locationEnabled);

        String type = (String) arrIntensity.get(0);
        String intDesc = (String) arrIntensity.get(1);
        String intColor = (String) arrIntensity.get(2);
        int intTextColor = (int) arrIntensity.get(3);

        intensityDescription.setBackgroundColor(Color.parseColor(intColor));
        intensityDescription.setTextColor(intTextColor);
        intensityDescription.setText(intDesc);

        if (type.equals("reported")){
            intensityTextStatus.setText("INTENSIDAD REPORTADA");
            intensityRepButton.setVisibility(View.INVISIBLE);
        }else if( type.equals("myLocation") ){
            intensityTextStatus.setText("INTENSIDAD ESTIMADA\nEN MI UBICACIÓN");
        }else if(type.equals("nearLocation")){
            intensityTextStatus.setText("INTENSIDAD ESTIMADA\nLugar Cercano");
        }else{
            intensityTextStatus.setText("INTENSIDAD ESTIMADA\nen "+ type);
        }

    }

    private void setUserLocation(){

        //First Trying to remove old marker and line
        try{
            myLocMarker.remove();
            line.remove();
            Log.e(TAG,"Marker and line removed");
        }catch (Exception e){
            //not possible to remove
            Log.e(TAG,"Removing marker and line error: "+e.toString());
        }

        //getting the new distance
        int distance = util.distanceTwoPoints(eq.getLat(),eq.getLon(), userLat, userLon );

        //int distance = util.distanceTwoPoints(eq.getLat(),eq.getLon(), 14.19177007306464, -88.01860047575258);
        Log.d(TAG,"Distance to user's location: "+distance+" km");

        //getting the backazimuth
        String backAz = util.findazimuth( userLat, userLon, eq.getLat(), eq.getLon() );
        //String backAz = util.findazimuth( 14.19177007306464, -88.01860047575258, eq.getLat(), eq.getLon() );
        Log.d(TAG,"BackAzimuth user's location: "+backAz);


        //Intensity
        int tmpInt = eqDbHelper.getMaxIntensityReported(evtid);
        Log.e(TAG,"Reported intensity: "+tmpInt);

        if (tmpInt <0){
            //No intensity reported
            tmpInt = util.ipe_allen2012_hyp(distance, eq.getMagnitude(),eq.getDepth());
            Log.i(TAG,"Estimated Intensity: "+tmpInt);
            intensityTextStatus.setText("INTENSIDAD ESTIMADA\nen mi ubicación");
        }else{
            Log.i(TAG,"Reported Intensity: "+tmpInt);
            intensityTextStatus.setText("Intensidad Reportada");
            intensityRepButton.setVisibility(View.INVISIBLE);
        }

        String intTmp = util.intensity2RomanDescription(tmpInt);

        Log.i(TAG,"Intensity Description: "+intTmp);

        String intDesc = "--";
        String intColor = "#FFFFFF";
        int intTextColor = Color.GRAY;
        String type = null;
        int nearPlaceDist = -1;

        try{
            nearPlaceDist = eq.getNearPlaceDist();
        }catch (Exception e){
            nearPlaceDist = (int) Float.parseFloat(eq.getLocation().split(" ")[0]);
        }

        List<Object> arrIntensity = util.getIntensityDescAndColor(sharedPref,eq.getLat(), eq.getLon(), eq.getDepth(),
                eq.getMagnitude(), nearPlaceDist, evtid, locationEnabled);

        type = (String) arrIntensity.get(0);
        intDesc = (String) arrIntensity.get(1);
        intColor = (String) arrIntensity.get(2);
        intTextColor = (int) arrIntensity.get(3);

        Log.d(TAG,"Type: "+type+" "+intDesc+" "+intColor+" "+intTextColor+" "+locationEnabled);
        intensityDescription.setBackgroundColor(Color.parseColor(intColor));
        intensityDescription.setTextColor(intTextColor);
        intensityDescription.setText(intDesc);


        //line from user location to epicenter
        line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(userLat, userLon), new LatLng(eq.getLat(), eq.getLon()))
                // .add(new LatLng(14.19177007306464, -88.01860047575258), new LatLng(eq.getLat(), eq.getLon()))
                .width(5)
                .color(Color.parseColor("#40000000")));
        List<PatternItem> pattern = Arrays.asList(
                new Dot(), new Gap(20), new Dash(30), new Gap(20));
        line.setPattern(pattern);

        //MARKER

        //adding the marker if it does not exit or update the location if it does
        LatLng latLng =new LatLng(userLat, userLon);
        if (mMap == null){
            Log.d(TAG,"Map not ready. Returning");
            return;
        }
        myMarkerOptions = new MarkerOptions();
        myMarkerOptions.position(latLng);
        if(intDesc.equals("--")){
            myMarkerOptions.title("Intensidad: --");
        }else{
            myMarkerOptions.title(intDesc);
        }


        myMarkerOptions.snippet("A "+distance + " km al "+backAz);
        myLocMarker = mMap.addMarker(myMarkerOptions);
        myLocMarker.setPosition(latLng);
        myLocMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_personlocation_enabled));


        locationEnabled = true;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("locationEnabled",true);
        editor.apply();
        enableLocationButton.setImageResource(R.mipmap.ic_personlocation_enabled);

        editor = sharedPref.edit();
        editor.putLong("userLat", Double.doubleToRawLongBits(userLat));
        editor.putLong("userLon", Double.doubleToRawLongBits(userLon));
        editor.putInt("lastUserLocationTime",util.utcNowTimestamp());
        long now = (long) util.getUnixTimestampFromTrueTime();

        if( now == 0 ){
            editor.putLong("lastUserLocationTimeMs",util.utcNowTimestampmsecs());
        }else{
            editor.putLong("lastUserLocationTimeMs",now);
        }
        editor.apply();
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


    /*
    *
    * GPS and LOCATION Methods
    *
    * */

    @SuppressLint("MissingPermission")
    private void getLoc(){

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(EqActivity.this, new OnSuccessListener <Location> () {
            @Override
            public void onSuccess(Location loc) {

                if (loc !=null){
                    Log.d(TAG,"Got location!!");
                    userLat =  loc.getLatitude();
                    userLon = loc.getLongitude();
                    Log.d(TAG,"GetLoc: lat: "+userLat+", Lon: "+userLon);
                    setUserLocation();

                }

            }
        });

    }

    private void enableGPS(){
        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        Task <LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());

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
                        resolvableApiException.startResolutionForResult(EqActivity.this,
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
    private void getCurrentGPSLocation() {

        getLoc();
        Log.d(TAG, "Request done");

        //Initialize location call back
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location =  locationResult.getLastLocation();
                Log.d(TAG,"Lat: "+ location.getLatitude()+", " +
                        "Lon: " + location.getLongitude() );
                userLat = location.getLatitude();
                userLon = location.getLongitude();

                //Saveing in memory this user's aproximate location
                util.setUserLocation(sharedPref,location.getLatitude(), location.getLongitude());
                setUserLocation();
            }
        };

        //Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(!requestingLocationUpdates){
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
            requestingLocationUpdates = true;
        }

    }

    private void stopLocationUpdates() {
        if(fusedLocationProviderClient != null){
            try{
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                requestingLocationUpdates = false;
                Log.d(TAG,"Removed Callback...");
            }catch (Exception e){
                Log.e(TAG,"Error removing the location updates: "+e.toString());
            }

        }

    }

}
