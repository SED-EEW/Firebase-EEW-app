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
* Activity that is used as a controller and model for obtaining
* the user's points of interest.
* The max number of points of interest can be set on
* MAX_POI_NUMBERS. This maximum value must be also set on Util class
*
* */
package com.bbr.attacapp.poi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bbr.attacapp.R;
import com.bbr.attacapp.mainactivity.MainActivity;
import com.bbr.attacapp.utilitary.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class UserPOI extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private Context context;
    private GoogleMap mMap;
    private Marker mLastMarker;
    private MarkerOptions mLastMarkerOptions;
    private final static String TAG = "UserPOI";
    private int counter=0;
    private Util util;
    private SharedPreferences sharedPref;
    private Button saveButton;
    private Button addButton ;
    private Button clearButton ;
    private Button cancelButton;
    private String anotherAct;

    private final int MAX_POI_NUMBERS = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point_of_interest);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_poi);

        mapFragment.getMapAsync(this);

        context = this;
        util = new Util(context);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean poiSet = sharedPref.getBoolean("poiIsSet", false);

        if (getIntent().getExtras() != null) {
            anotherAct = getIntent().getStringExtra("actName");
            Log.d(TAG, " activity Name => " + anotherAct);
        } else {
            //
        }

        if ( anotherAct == null && poiSet ){
            changeActivity(anotherAct);
        }


        // Button that saves the location user chose
        saveButton = (Button) findViewById(R.id.poi_save);
        addButton = (Button) findViewById(R.id.poi_addbtn);
        clearButton = (Button) findViewById(R.id.poi_clear);
        cancelButton = (Button) findViewById(R.id.poi_cancel);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( counter == 0 ){
                    Toast.makeText(context,"Agregar al menos un Punto de Interés",Toast.LENGTH_LONG).show();
                    return;
                }else{
                    setPoiSharedPref();
                    changeActivity(anotherAct);
                }
                try{

                    Log.e(TAG, "User chose: " + mLastMarker.getPosition().toString());
                }catch (Exception e){
                    Toast.makeText(context,"Haga click en la ubicación de interés",Toast.LENGTH_LONG).show();
                    return;
                }

            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( counter == MAX_POI_NUMBERS ){
                    return;
                }
                try{

                    dialogPOI();


                }catch (Exception e){
                    Toast.makeText(context,"Haga Click en la ubicación de interés",Toast.LENGTH_LONG).show();
                    return;
                }

                // Once the creation mode has ended remove the listener
                //mMap.setOnMapClickListener(null);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( counter > 0 ){
                    mMap.clear();
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    util.clearUserPOIs(sharedPref);
                    counter = 0;
                    saveButton.setText("Guardar");
                    saveButton.setClickable(true);
                    //saveButton.setBackgroundColor(Color.GRAY);
                    clearButton.setEnabled(false);
                    addButton.setVisibility(View.VISIBLE);
                    return;
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPoiSharedPref();
                changeActivity(anotherAct);
            }
        });




    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //ok, map ready

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        zoomControl();

        // checking there are some POIs on memory already selected by the user
        List<Object> poiObj = null;
        String name = null;
        LatLng latLng = null;
        LatLng lastLatLong = null;

        double poiLatTmp = -999.00;
        double poiLonTmp = -999.00;


        if ( sharedPref == null){
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        }
        for( int i=1; i<=MAX_POI_NUMBERS; i++ ){
           poiObj = util.getUserPOIs(sharedPref, i);

           latLng =  (LatLng) poiObj.get(0);
           name =  (String) poiObj.get(1);


           poiLatTmp = latLng.latitude;
           poiLonTmp = latLng.longitude;

           if ( !name.equals("") ) {
               //adding this marker
               Log.d(TAG,"From memory -> name: "+name+", lat: "+poiLatTmp + ", lon: "+poiLonTmp);
               mLastMarkerOptions = new MarkerOptions().position(latLng);
               mLastMarker = mMap.addMarker(mLastMarkerOptions);
               mLastMarker.setTitle(name);
               mLastMarker.showInfoWindow();
               counter += 1;
               lastLatLong = latLng;
           }else{
               //poi is not valid
           }

        }
        if ( counter > 0 ){
            //:pg
            addButton.setVisibility(View.INVISIBLE);
            clearButton.setEnabled(true);
            cancelButton.setText("Salir");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLong, 5.6f));
            if( counter == MAX_POI_NUMBERS ){
                saveButton.setClickable(false);
            }
        }

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //get latlng at the center by calling
                LatLng midLatLng = mMap.getCameraPosition().target;
                Log.d(TAG,"IdleListener says: Lat="+midLatLng.latitude+", Lon = "+midLatLng.longitude);
                mLastMarkerOptions = new MarkerOptions().position(midLatLng);
                addButton.setVisibility(View.INVISIBLE);
                if ( counter == MAX_POI_NUMBERS ){
                    return;
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        addButton.setVisibility(View.VISIBLE);
                    }
                }, 100); //time in millis
            }
        });
    }

    public void dialogPOI(){
        final EditText taskEditText = new EditText(context);
        taskEditText.setHint(R.string.point_of_interest_hint);
        int maxLength = 15;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        taskEditText.setFilters(filters);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Nombre de Punto de Interés")
                .setMessage("Ingrese un nombre que identifique al punto de interés...")
                .setView(taskEditText)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = String.valueOf(taskEditText.getText());

                        if( name.equals("")){
                            Toast.makeText(context, R.string.point_of_interest_toast_lacking_name,Toast.LENGTH_LONG).show();
                        }else{
                            //saving the name and the whole POI info
                            counter +=1;
                            Log.d(TAG,"POI: "+name+", number:" + counter);
                            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                            util.setUserPOI(sharedPref,mLastMarkerOptions.getPosition(),counter,name);

                            mLastMarker = mMap.addMarker(mLastMarkerOptions);
                            mLastMarker.setTitle(name);
                            mLastMarker.showInfoWindow();
                            Log.e(TAG, "User chose: " + mLastMarker.getPosition().toString());
                            saveButton.setText("Guardar ("+counter+")");
                            if(counter > 0){
                                clearButton.setEnabled(true);
                            }
                            if(counter == MAX_POI_NUMBERS ){
                                addButton.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                })
                .setNegativeButton("cancelar", null)
                .create();
        dialog.show();
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

    public void changeActivity(String actName){

        if( actName == null || actName.equals("")){
            Intent i = new Intent(UserPOI.this, MainActivity.class);
            Log.d(TAG,"starting a new activity: MainActivity");
            startActivity(i);
            finish();
        }else if(actName != null){
            finish();
        }
    }

    public void setPoiSharedPref(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("poiIsSet", true);
        editor.apply();
    }


}
