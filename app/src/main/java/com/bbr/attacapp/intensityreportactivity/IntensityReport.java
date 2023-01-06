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
* This class is the Controller for Intensity Report Layout.
* It loads the cartoons and allows user to choose an intensity
* value based on these.
*
* */


package com.bbr.attacapp.intensityreportactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import androidx.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bbr.attacapp.utilitary.EqInfo;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;


public class IntensityReport extends AppCompatActivity implements View.OnClickListener {

    private Util util;
    private EqDbHelper eqDbHelper;
    private Context context;
    private final String TAG = "IntensityReport";
    private String evtid;
    private int intensity;
    private EqInfo eq;
    private Double lat = -999.0;
    private Double lon = -999.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final int NUM_UPDATES = 8;
    LocationRequest locationRequest;
    private boolean requestingLocationUpdates;
    private final int REQUEST_CHECK_SETTINGS = 0x1;
    private SharedPreferences myPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intensity_report);
        util = new Util(this);
        eqDbHelper = new EqDbHelper(this);
        context = this;

        if (getIntent().getExtras() != null) {
            evtid = getIntent().getStringExtra("evtid");
            Log.d(TAG, " evtid => " + evtid);
        } else {
            //
        }

        requestingLocationUpdates = false;//

        loadEq();

        ImageView one = (ImageView) findViewById(R.id.IntRepOne);
        one.setOnClickListener(this);
        ImageView two = (ImageView) findViewById(R.id.IntRepTwo);
        two.setOnClickListener(this);
        ImageView three = (ImageView) findViewById(R.id.IntRepThree);
        three.setOnClickListener(this);
        ImageView four = (ImageView) findViewById(R.id.IntRepFour);
        four.setOnClickListener(this);
        ImageView five = (ImageView) findViewById(R.id.IntRepFive);
        five.setOnClickListener(this);
        ImageView six = (ImageView) findViewById(R.id.IntRepSix);
        six.setOnClickListener(this);
        ImageView seven = (ImageView) findViewById(R.id.IntRepSeven);
        seven.setOnClickListener(this);
        ImageView eight = (ImageView) findViewById(R.id.IntRepEight);
        eight.setOnClickListener(this);
        ImageView nine = (ImageView) findViewById(R.id.IntRepNine);
        nine.setOnClickListener(this);
        ImageView ten = (ImageView) findViewById(R.id.IntRepTen);
        ten.setOnClickListener(this);
        ImageView eleven = (ImageView) findViewById(R.id.IntRepEleven);
        eleven.setOnClickListener(this);
        ImageView twelve = (ImageView) findViewById(R.id.IntRepTwelve);
        twelve.setOnClickListener(this);



        locationRequest = util.createLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                this);
        askPermission();
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG,"OnResume...");

        if(!requestingLocationUpdates){
            getCurrentGPSLocation();
        }
    }

    @Override
    protected  void onStart(){
        super.onStart();
        Log.d(TAG,"onStart...");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.e(TAG,"OnPause...");
        stopLocationUpdates();
    }

    public void showDialogLocationPermission() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("PERMISO DE UBICACIÓN APROXIMADA");
        builder.setMessage("Para reportar la intensidad se necesita conocer la ubicación APROXIMADA.\n" +
                "NO se usa la ubicación PRECISA.");
        builder.setIcon(R.mipmap.ic_attac);

        builder.setPositiveButton("De acuerdo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //user decided to set this value.
                ActivityCompat.requestPermissions(IntensityReport.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("Reportar sin ubicación", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == 1 && grantResults[0] >= 0 ) {
            Log.d(TAG, "onRequestPermissionsResult: LOCATION PERMISSION successful");
            enableGPS();
        }else{
            Log.d(TAG,"Not granted for location");
        }
    }

    @Override
    public void onClick(View v) {
        int intensityVal =0;
        Log.d(TAG,"Clicked on Intensity value: "+v.getId());
        switch ( v.getId() ){
            case R.id.IntRepOne:
                intensityVal=1;
                break;
            case R.id.IntRepTwo:
                intensityVal=2;
                break;
            case R.id.IntRepThree:
                intensityVal=3;
                break;
            case R.id.IntRepFour:
                intensityVal=4;
                break;
            case R.id.IntRepFive:
                intensityVal=5;
                break;
            case R.id.IntRepSix:
                intensityVal=6;
                break;
            case R.id.IntRepSeven:
                intensityVal=7;
                break;
            case R.id.IntRepEight:
                intensityVal=8;
                break;
            case R.id.IntRepNine:
                intensityVal=9;
                break;
            case R.id.IntRepTen:
                intensityVal=10;
                break;
            case R.id.IntRepEleven:
                intensityVal=11;
                break;
            case R.id.IntRepTwelve:
                intensityVal=12;
                break;
            default:
                break;

        }
        Log.d(TAG,"Selected Intensity: "+intensityVal);
        confirmationDialog(intensityVal);



    }

    private void loadEq() {
        Cursor evtCursor = eqDbHelper.getEqById(evtid);
        if (evtCursor.getCount() >= 1) {
            evtCursor.moveToNext();
        } else {
            Toast.makeText(context, "El evento ya NO existe.", Toast.LENGTH_SHORT).show();
            //closing the activity
            finish();
            return;
        }
        try {
            eq = new EqInfo(evtCursor);
        } catch (Exception e) {
            Log.e(TAG, "Event with ID:" + evtid + " does not exist. Leaving");
            Toast.makeText(context, "El evento ya NO existe.", Toast.LENGTH_SHORT).show();
            finish();
            return;

        }
    }

    public void confirmationDialog(int intensityVal){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String tmp = util.intensity2RomanDescription(intensityVal);
        String tmpDescription = tmp.split(";")[0];
        String nearplace = eq.getLocation();
        float magnitude = eq.getMagnitude();
        float depth = eq.getDepth();
        float epiDistance = 0;
        int tmpIntensity = -1;

        builder.setTitle(tmpDescription);

        try{
            epiDistance = eq.getNearPlaceDist();
        }catch (Exception e){
            Log.e(TAG,e.toString());
            epiDistance = Float.parseFloat(nearplace.split(" ")[0]);
        }

        if( epiDistance>0){
            tmpIntensity = util.ipe_allen2012_hyp(epiDistance, magnitude, depth);
        }
        if (tmpIntensity>0){
            if (Math.abs( intensityVal - tmpIntensity) > 4){
                if(intensityVal > tmpIntensity){
                    builder.setMessage("Parece que el valor seleccionado está fuera de un rango aproximado.\nDesea registrar de todos modos esta intensidad?");
                }else{
                    builder.setMessage("Está seguro de registrar esta intensidad?");
                }

            }else{
                builder.setMessage("Registrar esa Intensidad?");
            }
        }else{
            builder.setMessage("Registrar esa Intensidad?");
        }
        switch (intensityVal){
            case 1:
                builder.setIcon(R.drawable.i);
                break;
            case 2:
                builder.setIcon(R.drawable.ii);
                break;
            case 3:
                builder.setIcon(R.drawable.iii);
                break;
            case 4:
                builder.setIcon(R.drawable.iv);
                break;
            case 5:
                builder.setIcon(R.drawable.v);
                break;
            case 6:
                builder.setIcon(R.drawable.vi);
                break;
            case 7:
                builder.setIcon(R.drawable.vii);
                break;
            case 8:
                builder.setIcon(R.drawable.viii);
                break;
            case 9:
                builder.setIcon(R.drawable.ix);
                break;
            case 10:
                builder.setIcon(R.drawable.x);
                break;
            case 11:
                builder.setIcon(R.drawable.xi);
                break;
            case 12:
                builder.setIcon(R.drawable.xii);
                break;
            default:
                break;
        }

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                intensity = intensityVal;

                Log.d(TAG,"Saving on Firestore the next information: \nLat: "+lat+", Lon: "+lon+", Intensity:"+intensity);
                Log.d(TAG,"This is linked to update number:"+eq.getUpdate()+" for event: " +eq.getEvtId());
                eq.setIntreported(intensity);
                eq.setUserlat(lat);
                eq.setUserlon(lon);

                new EqUpdateAddTask(context).execute();

                //the location is not available
                if (lat == -999.00 || lon == -999.00) {
                    Toast.makeText(IntensityReport.this, "Intensidad registrada SIN ubicación. Gracias", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(IntensityReport.this, "Gracias!", Toast.LENGTH_LONG).show();

                }
                //firestore update
                Map<String, Object> datainfo = new HashMap<>();
                datainfo.put("diff",  (eq.getRecTime()-eq.getSentTime())/1000.);
                datainfo.put("intensity", eq.getIntreported());
                datainfo.put("lat", eq.getUserlat() );
                datainfo.put("lon", eq.getUserlon() );
                datainfo.put("timestamp",util.utcNowTimestamp());

                myPrefs = PreferenceManager.getDefaultSharedPreferences(IntensityReport.this);
                String android_key = myPrefs.getString("deviceid", "NOT_SET");

                boolean tmpBool = util.saveOnFirestore(eq.getEvtId(),eq.getUpdate(),android_key,datainfo);
                if( !tmpBool ){
                    Log.e(TAG, "removing the intensity value to be reported to Firestore.");
                    Toast.makeText(IntensityReport.this, "Hubo un error registrando la intensidad. Por favor, inténtelo más tarde.", Toast.LENGTH_LONG).show();
                    eq.setIntreported(-1);
                    eq.setUserlat(lat);
                    eq.setUserlon(lon);
                }
                dialog.dismiss();
                finish();

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                //finish();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }



    private class EqUpdateAddTask extends AsyncTask<Void, Void, Integer> {

        public EqUpdateAddTask (Context context){
            context = context;
        }


        @Override
        protected Integer doInBackground(Void... voids) {
            int updateno = eq.getUpdate();
            int tmpVal = eqDbHelper.updateReportedIntensity(eq,intensity,evtid,updateno);

            return tmpVal;
        }

        @Override
        protected void onPostExecute(Integer response) {

            return;
        }

    }

    private void askPermission(){

        //Pemission - Location
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            Log.d(TAG,"Permission for approximated location is granted");
            //getting the location
            enableGPS();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ) {
                // In an educational UI, explain to the user why your app requires this
                // permission.
                Log.d(TAG,"Permission not allowed. asking politely");
                showDialogLocationPermission();
            } else {
                // directly asking for the permission.
                Log.d(TAG,"User does not allow to use the location.");
                //just in case asking for permission.
                //most likely only the dialog will appear but not
                //the permission request
                showDialogLocationPermission();
            }
        }

    }

    @SuppressLint("MissingPermission")
    private void getLoc(){

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(IntensityReport.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location loc) {

                if (loc !=null){
                    Log.d(TAG,"Got location!!");
                    lat =  loc.getLatitude();
                    lon = loc.getLongitude();
                    Log.d(TAG,"GetLoc: lat: "+lat+", Lon: "+lon);
                    //setUserLocation();

                }

            }
        });

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
                        resolvableApiException.startResolutionForResult(IntensityReport.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();

                    }
                }
            }
        });
    }

    private void getCurrentGPSLocation() {

        getLoc();
        Log.d(TAG, "Request done");

        //Initialize location call back
        locationCallback = new LocationCallback() {
            @Override

            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "Callback started");
                //Initialize location1
                Location location1 = locationResult.getLastLocation();

                //Set latitude
                lat = location1.getLatitude();
                //Set longitude
                lon = location1.getLongitude();
                Log.d(TAG,"Location Callback. lat: "+lat+", Lon: "+lon);
                myPrefs = PreferenceManager.getDefaultSharedPreferences(IntensityReport.this);
                util.setUserLocation(myPrefs, lat, lon);
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
