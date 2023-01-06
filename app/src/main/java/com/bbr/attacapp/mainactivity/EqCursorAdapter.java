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
* This is an adapter for the elements obtained from the local DB.
*
* */
package com.bbr.attacapp.mainactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqContract;
import com.bbr.attacapp.dbsqlite.EqContract.EqEntry;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.utilitary.Util;

import java.util.List;

public class EqCursorAdapter extends CursorAdapter {

    Util util ;
    private EqDbHelper mEqDbHelper;
    boolean isUTC = false;
    private final String TAG = "EQcursorAdapter";
    private SharedPreferences myPrefs;
    private double userLat;
    private double userLon;
    private int lastUserLocationTime ;
    private long lastUserLocationTimeMs;
    private boolean locationEnabled;
    private Context context;

    public EqCursorAdapter(Context cxt, Cursor c, boolean locEnabled) {
        super(cxt, c, 0);

        context = cxt;
        util = new Util(context);
        isUTC = util.readPrefSwitch(context, "prefUTCSwitch", "default");
        Log.d(TAG,"Boolean UTC:"+isUTC);
        mEqDbHelper = new EqDbHelper(context);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        userLat = Double.longBitsToDouble(myPrefs.getLong("userLat", 0));
        userLon = Double.longBitsToDouble(myPrefs.getLong("userLon", 0));
        lastUserLocationTime = myPrefs.getInt("lastUserLocationTime",0);
        lastUserLocationTimeMs = myPrefs.getLong("lastUserLocationTimeMs",0);
        locationEnabled = locEnabled;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.eq_item, viewGroup, false);
    }


    @SuppressLint("Range")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        boolean estimated=false;
        String dt = "";
        TextView tvNearPlace = (TextView) view.findViewById(R.id.EqUpLocationVal);
        tvNearPlace.setSelected(true);
        @SuppressLint("Range") String location = cursor.getString(cursor.getColumnIndex(EqEntry.LOCATION));
        tvNearPlace.setText(location);

        //Magnitude
        TextView tvMagVal = (TextView) view.findViewById(R.id.ItemMagVal);
        //SuppressLint used to avoid potential errors(?) (maybe it can be the other side... an error!)
        @SuppressLint("Range") float magnitude = cursor.getFloat(cursor.getColumnIndex(EqEntry.MAGNITUDE));
        @SuppressLint("Range") float epiLat = cursor.getFloat(cursor.getColumnIndex(EqEntry.LAT));
        @SuppressLint("Range") float epiLon = cursor.getFloat(cursor.getColumnIndex(EqEntry.LON));

        int eqDist = -1;
        try {
            eqDist = cursor.getInt(cursor.getColumnIndex(EqEntry.DISTNEARLOC));
        }catch ( Exception e){
            Log.e(TAG,e.toString());
        }

        if(eqDist == -1){
            eqDist = (int) Float.parseFloat(location.split(" ")[0]);
        }

        tvMagVal.setText("Magnitud: "+String.format("%.1f",magnitude));
        if ( magnitude < 4.5 ){
            tvMagVal.setBackgroundColor(Color.GRAY);
        }else if( magnitude >= 4.5 && magnitude < 5.8 ){
            tvMagVal.setBackgroundColor(Color.parseColor("#FFA500"));
        }else{
            tvMagVal.setBackgroundColor(Color.RED);
        }

        //Depth
        TextView tvDepthVal = (TextView) view.findViewById(R.id.EqUpDepthVal);
        @SuppressLint("Range") float depth = cursor.getFloat(cursor.getColumnIndex(EqEntry.DEPTH));
        tvDepthVal.setText(String.format("%.0f",depth)+" km de Profundidad");
        //Log.d(TAG,"ID for this event:"+cursor.getString(cursor.getColumnIndex(EqEntry._ID)));

        //OriginTime
        TextView tvOrTimeVal = (TextView) view.findViewById(R.id.EqUpDateTimeVal);
        @SuppressLint("Range") int orTimeUnix = cursor.getInt(cursor.getColumnIndex(EqEntry.ORTIME));
        if ( isUTC ){
            dt = util.utcTimestamp2utcISO8601(orTimeUnix);
            tvOrTimeVal.setText("Fecha y Hora UTC: "+dt);
        }else{
            dt = util.utcTimestamp2localISO8601(orTimeUnix);
            tvOrTimeVal.setText("Fecha y Hora Local: "+dt);
        }


        //now - or time
        TextView tvTimeafter = (TextView) view.findViewById(R.id.EqUpLatLonVal);
        String tmp = util.getTimeSpanFromNow(orTimeUnix);
       // String agency = cursor.getString(cursor.getColumnIndex(EqEntry.AGENCY));
        tvTimeafter.setText("Ocurrido hace: " + tmp);

        //Agency
        TextView tvAgency = (TextView) view.findViewById(R.id.EqUpSentVal);
        @SuppressLint("Range") String agency = cursor.getString(cursor.getColumnIndex(EqEntry.AGENCY));
        agency = agency.toUpperCase();
        if( agency.equals("UNA")) {
            agency = "OVSICORI-UNA";
        }
        tvAgency.setText("Fuente: "+agency);

        @SuppressLint("Range") int test = cursor.getInt(cursor.getColumnIndex(EqContract.EqEntry.NUMARRIVALS));

        //Intensity
        @SuppressLint("Range") String evtid = cursor.getString(cursor.getColumnIndex(EqEntry.EVTID));

        String intDesc = "--";
        String intColor =  "#FFFFFF";
        int intTextColor = Color.GRAY;

        List<Object> arrIntensity = util.getIntensityDescAndColor(myPrefs, epiLat, epiLon, depth, magnitude, eqDist, evtid, locationEnabled);
        String type = (String) arrIntensity.get(0);
        intDesc = (String) arrIntensity.get(1);
        intColor = (String) arrIntensity.get(2);
        intTextColor = (int) arrIntensity.get(3);

        TextView intTV = (TextView) view.findViewById(R.id.EqUpIntensityVal);
        Log.d(TAG,"TYPE: "+type);

        if( !type.equals("reported") ){

            if(type.equals("myLocation")){

                if( intDesc.equals("--") ) {
                    intTV.setText("Sin Intensidad (en su ubicación)");
                }else{
                    intTV.setText(intDesc + " (en su ubicación)");
                }

            }else if( type.equals("nearLocation")){

                intTV.setText(intDesc + " (lugar cercano)");

            }else{

                    intTV.setText(intDesc + " (en "+type+")");

            }

        }
        else{

            intTV.setText(intDesc+" (Reportada)");

        }
        intTV.setTextColor(intTextColor);
        intTV.setBackgroundColor(Color.parseColor(intColor));

        intTV.setTypeface(intTV.getTypeface(), Typeface.BOLD);

    }
}
