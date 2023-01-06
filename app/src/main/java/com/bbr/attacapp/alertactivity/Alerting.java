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
* This class is a controller for alert.xml layout.
* It is to present an alert message on the user's screen
* to Drop, cover and Hold on and information about an EQ alert in progress.
* It needs the eventID which obtained from the Intent's extra information.
* See more details in how this class is used on MyFirebaseInstanceService.class
*
* */

package com.bbr.attacapp.alertactivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.eqactivity.EqActivity;
import com.bbr.attacapp.utilitary.EqInfo;
import com.bbr.attacapp.utilitary.Util;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.List;

public class Alerting extends AppCompatActivity implements OnMapReadyCallback {
    private SupportMapFragment mapFragment;
    private Context context;
    private String evtid;
    private String TAG = "Alerting" ;
    private EqDbHelper eqDbHelper;
    private EqInfo eq;
    private Util util;
    private SharedPreferences sharedPreferences;
    private TextView intensityDescription;
    private TextView distanceDescription;
    private TextView intensityValue;
    private TextView magValue;
    private TextView depthValue;
    private TextView sourceValue;
    private GoogleMap mMap;
    private MarkerOptions myMarkerOptions;
    private LatLng markerLoc = null;
    private Marker myLocMarker;
    private Polyline line;
    private Button detailsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.alertmap);
        mapFragment.getMapAsync(this);
        context = this;
        getSupportActionBar().hide();

        if(getIntent().getExtras() != null){
            evtid = getIntent().getStringExtra("evtid");
            Log.d(TAG," evtid => " + evtid);
        }
        else{
            //event does not exist!
            finish();
        }


        eqDbHelper = new EqDbHelper(context);
        util = new Util(context);

        intensityValue = (TextView) findViewById(R.id.alertIntensityValue);
        intensityDescription = (TextView) findViewById(R.id.alertIntensityDescription);
        distanceDescription = (TextView) findViewById(R.id.alertDistanceDescription);
        magValue = (TextView) findViewById(R.id.alertMagValue);
        depthValue = (TextView) findViewById(R.id.alertDepthValue);
        sourceValue = (TextView) findViewById(R.id.alertSourceValue);
        detailsButton = (Button) findViewById(R.id.alertButton);

        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Alerting.this, EqActivity.class);

                i.putExtra("evtid", eq.getEvtId());
                startActivity(i);
                finish();
            }
        });

        loadEq();




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
        Log.d(TAG,"OnPause...");

    }

    public void loadEq(){
        Cursor evtCursor =eqDbHelper.getEqById(evtid);

        if(evtCursor.getCount() >= 1) {
            evtCursor.moveToNext();
        }else{
            Toast.makeText(context,getString(R.string.eventdoesnotexist),Toast.LENGTH_SHORT).show();

            finish();
            return;
        }
        try{
            eq = new EqInfo(evtCursor);
        }catch (Exception e){
            Log.e(TAG,"Event with ID:"+evtid+" does not exist. Leaving");
            Toast.makeText(context,getString(R.string.eventdoesnotexist),Toast.LENGTH_SHORT).show();
            finish();
            return;

        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isGPSenabled = util.isGPSenabled(context);

        List<Object> obj = util.getIntensityDescAndColor(sharedPreferences,eq.getLat(),eq.getLon(),eq.getDepth(),eq.getMagnitude(),eq.getNearPlaceDist(),evtid,isGPSenabled);

        String type = (String) obj.get(0);
        String intDesc = (String) obj.get(1);
        String intColor = (String) obj.get(2);
        int intTextColor = (int) obj.get(3);

        int distance = 0;
        String backAzim = "";

        util = new Util(context);


        if(type.equals("reported")){
            //this should not happen but in case this is true, then the layout is closed.
            finish();
        }else{
            if(type.equals("myLocation")){

                if( intDesc.equals("--") ) {
                    intensityValue.setText("Sin Intensidad\n(en su ubicación)");
                }else{
                    intensityValue.setText(intDesc );
                }
                intensityDescription.setText(getString(R.string.alert_intesitytitle)+" "+getString(R.string.yourlocation));
                //distance
                List<Object> userLoc = util.getUserLocation(sharedPreferences);
                LatLng userLatLon = (LatLng) userLoc.get(0);
                long userLastTimeMs = (long) userLoc.get(2);
                distance = util.distanceTwoPoints(eq.getLat(),eq.getLon(), userLatLon.latitude, userLatLon.longitude);
                backAzim = util.findazimuth(userLatLon.latitude, userLatLon.longitude,eq.getLat(),eq.getLon());

                markerLoc = userLatLon;

                distanceDescription.setText(distance+" km "+getString(R.string.to_the)+" "+backAzim+" " + getString(R.string.of)+" " + getString(R.string.yourlocation));

            }else if( type.equals("nearLocation")){

                intensityValue.setText(intDesc);
                intensityDescription.setText(getString(R.string.alert_intesitytitle)+" "+getString(R.string.nearlocation));
                distanceDescription.setText(eq.getLocation());

            }else{
                List<Object> maxPoi = util.getMaxIntensityOnPOI(sharedPreferences, eq);
//                int poiIntVal = (int) maxPoi.get(2);
                String poiName  = (String) maxPoi.get(1);
                LatLng poiLoc = (LatLng) maxPoi.get(0);

                distance = util.distanceTwoPoints(eq.getLat(), eq.getLon(), poiLoc.latitude, poiLoc.longitude);
                backAzim =  util.findazimuth(poiLoc.latitude, poiLoc.longitude,eq.getLat(),eq.getLon());
                intensityValue.setText(intDesc);
                intensityDescription.setText(getString(R.string.alert_intesitytitle)+" "+type);
                distanceDescription.setText(distance+" km "+getString(R.string.to_the)+" "+backAzim+" " + getString(R.string.of)+" " + poiName);
                markerLoc = poiLoc;

            }

            intensityValue.setBackgroundColor(Color.parseColor(intColor));
            intensityValue.setTextColor(intTextColor);

        }

        float magVal = eq.getMagnitude();
        magValue.setText(magVal+"");
        if ( magVal < 4.5 ){
            magValue.setBackgroundColor(Color.GRAY);
        }else if( magVal >= 4.5 && magVal < 5.8 ){
            magValue.setBackgroundColor(Color.parseColor("#FFA500"));
        }else{
            magValue.setBackgroundColor(Color.RED);
        }

        float depth = eq.getDepth();
        depthValue.setText(depth+"" );

        String a = eq.getAgency().toUpperCase();
        if( a.equals("UNA")) {
            a = "OVSICORI-UNA";
        }

        sourceValue.setText(getString(R.string.source)+": " +a);



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        //setting markers and distance
        mMap = googleMap;
        if(eq == null ){
            return;
        }
        //Map controllers - enabling...
       // mMap.getUiSettings().setZoomControlsEnabled(true);

        try{
            //line.remove();
            //myLocMarker.remove();
            mMap.clear();
        }catch (Exception e){
            Log.e(TAG,"Not possible to clear map. See below...");
            Log.e(TAG,e.toString());
        }

        LatLng newLatLng = new LatLng(eq.getLat(),eq.getLon());
        Marker m = null;
        MarkerOptions markerOptions = new MarkerOptions();
        CircleOptions circleOptions = new CircleOptions();

        circleOptions.center(newLatLng).radius(eq.getMagnitude()*2000);
        circleOptions.fillColor(Color.RED).strokeColor(Color.BLACK).strokeWidth(1);
        Circle c = mMap.addCircle(circleOptions);

        //User location or POI
        if( markerLoc != null){

            myMarkerOptions = new MarkerOptions();
           // com.google.android.gms.maps.model.LatLng markerLocGoogle = new com.google.android.gms.maps.model.LatLng(markerLoc.getLatitude(), markerLoc.getLongitude());
            myMarkerOptions.position(markerLoc);
            myLocMarker = mMap.addMarker(myMarkerOptions);
            myLocMarker.setPosition(markerLoc);
            myLocMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_personlocation_enabled));

            //line
            line = mMap.addPolyline(new PolylineOptions()
                    .add(markerLoc, new LatLng(eq.getLat(), eq.getLon()))
                    // .add(new LatLng(14.19177007306464, -88.01860047575258), new LatLng(eq.getLat(), eq.getLon()))
                    .width(5)
                    .color(Color.parseColor("#40000000")));
            List<PatternItem> pattern = Arrays.asList(
                    new Dot(), new Gap(20), new Dash(30), new Gap(20));
            line.setPattern(pattern);

        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,7.2f));


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
                Toast.makeText(context,getString(R.string.newalertreceived),Toast.LENGTH_SHORT).show();
            }
            loadEq();
        }
    };
}
