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
* This class is for the updates information layout on the app.
* It loads every update for one solution and present them on the
* layout.
*
* */

package com.bbr.attacapp.equpdatesactivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bbr.attacapp.utilitary.EqInfo;
import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqContract;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class EqUpdatesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EqUpdateAdapter mEqUpdateAdapter;
    private ListView listview;
    private EqDbHelper mEqDbHelper;
    private String TAG = "EqUpdateActivity";
    private Context context;
    private String evtid;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private List<Circle> listCircles;
    private ImageView logoView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equpdates_layout);
        if(getIntent().getExtras() != null){
            evtid = getIntent().getStringExtra("evtid");
            Log.d(TAG," evtid => " + evtid);
        }
        else{
            //
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.eqmapupdates);
        mapFragment.getMapAsync(this);
        //listview = (ListView) findViewById(R.id.eqmapupdates);
        listview = (ListView) findViewById(R.id.lvEqUpdates);
        context = this;
        mEqDbHelper = new EqDbHelper(this);
        //Evt Title
        TextView title = (TextView) findViewById(R.id.EqUpItemEvtidVal);
        title.setText("Evento "+evtid);

        logoView = (ImageView) findViewById(R.id.EqUpdateLogo);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view, int pos, long id){
                new EqTask().execute();
                return;
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (mMap != null){
            Log.d(TAG,"Clearing the markers from the map");
            mMap.clear();
        }else{
        //    return;
        }
        loadEqs();
    }

    private void loadEqs() {
        if (listview.getCount() > 0){
            listview.setAdapter(null);
            Log.d(TAG,"clearing the data on listview");
        }else{
            Log.d(TAG,"listview is empty");
        }

        mEqUpdateAdapter = new EqUpdateAdapter(this, null);
        listview.setAdapter(mEqUpdateAdapter);
        new EqUpdatesActivity.EqLoadTask().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setPadding(0,0,800,0);

    }

    private class EqLoadTask extends AsyncTask<Void, Void, Cursor> {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Cargando datos. Espere, por favor....");

            pd.show();
        }
        @Override
        protected Cursor doInBackground(Void... voids) {
            Cursor cursor;

            cursor = mEqDbHelper.getEqAllById(evtid);

            return cursor;
        }

        @SuppressLint("Range")
        @Override
        protected void onPostExecute(Cursor cursor) {
            mEqUpdateAdapter.swapCursor(cursor);
            LatLng newLatLng = null;
            listCircles = new ArrayList<>();
            String agency = null;
            if (cursor != null && cursor.getCount() > 0) {

                for (cursor.moveToFirst();
                     !cursor.isAfterLast();
                     cursor.moveToNext()){
                //for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
                    Marker m = null;
                    MarkerOptions markerOptions = null;
                    float latitude = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.LAT));
                    float longitude = cursor.getFloat(cursor.getColumnIndex(EqContract.EqEntry.LON));
                    int updateNumber = cursor.getInt(cursor.getColumnIndex(EqContract.EqEntry.UPDATENO));
                    newLatLng = new LatLng(latitude, longitude);
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(newLatLng).radius(2.5*2000);
                    circleOptions.fillColor(Color.RED).strokeColor(Color.BLACK).strokeWidth(1);

                    Circle c = mMap.addCircle(circleOptions);

                    CircleTag ct = new CircleTag(updateNumber);
                    c.setTag(ct);

                    agency= cursor.getString(cursor.getColumnIndex(EqContract.EqEntry.AGENCY)).toUpperCase();

                }

                if( agency.equals("UNA")) {
                    agency = "OVSICORI-UNA";
                    logoView.setImageResource(R.mipmap.ic_ovsicori);
                }
                if( agency.equals("INETER")) {
                    //a = "OVSICORI-UNA";
                    logoView.setImageResource(R.mipmap.ic_ineter);
                }
                if( agency.equals("MARN")) {
                    logoView.setImageResource(R.mipmap.ic_marn);
                }
                if( agency.equals("INSIVUMEH")) {
                    logoView.setImageResource(R.mipmap.ic_insivumeh);
                }


                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));

                //mMap.animateCamera(CameraUpdateFactory.zoomTo(7.5f));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,7.5f));


            }
            pd.dismiss();
        }
    }



    private class EqTask extends AsyncTask<Void, Void, Cursor> {
        private EqInfo eq;
        @Override
        protected Cursor doInBackground(Void... voids) {
            Cursor cursor = mEqDbHelper.getAllEqs();
            return cursor;
        }

        @Override
        @SuppressLint("Range")
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {

                mMap.clear();
                Cursor eqCursor = (Cursor)mEqUpdateAdapter.getCursor();

                eq = new EqInfo(eqCursor);
                /* Filling the last eq info */
                Log.d("INFO:","Mag: " +eq.getMagnitude());
                //showEq();
                int updateNumber = eq.getUpdate();
                Log.d(TAG,"NEW Update Number is:" +updateNumber);

                Cursor allEqs = mEqDbHelper.getEqAllById(eq.getEvtId());
                CircleOptions selectedEvt = null;
                if (allEqs != null && allEqs.getCount() > 0) {

                    for (allEqs.moveToFirst();
                         !allEqs.isAfterLast();
                         allEqs.moveToNext()){
                        //for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()){
                        int currentUpNumber = allEqs.getInt(allEqs.getColumnIndex(EqContract.EqEntry.UPDATENO));

                        float latitude = allEqs.getFloat(allEqs.getColumnIndex(EqContract.EqEntry.LAT));
                        float longitude = allEqs.getFloat(allEqs.getColumnIndex(EqContract.EqEntry.LON));

                        LatLng newLatLng = new LatLng(latitude, longitude);
                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(newLatLng).radius(2.5*2000);
                        if( currentUpNumber == updateNumber){
                            circleOptions.fillColor(Color.parseColor("#FF0000")).strokeColor(Color.BLACK).strokeWidth(1);
                            selectedEvt = circleOptions;
                        }else{
                            circleOptions.fillColor(Color.parseColor("#50C1C1C1")).strokeColor(Color.BLACK).strokeWidth(1);
                            Circle c = mMap.addCircle(circleOptions);
                            CircleTag ct = new CircleTag(updateNumber);
                            c.setTag(ct);

                        }

                    }
                    if ( selectedEvt!= null ){

                        Circle c = mMap.addCircle(selectedEvt);
                        CircleTag ct = new CircleTag(updateNumber);
                        c.setTag(ct);

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedEvt.getCenter(),8.5f));
                    }

                }

                return;
            }
        }
    }

    private class CircleTag{
        private int updateNumber;

        public CircleTag(int update){
            this.updateNumber = update;
        }

        public int getUpdateNumber(){
            return updateNumber;
        }
    }
}
