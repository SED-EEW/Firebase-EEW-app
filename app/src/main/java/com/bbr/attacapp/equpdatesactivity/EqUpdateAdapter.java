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
 * This is an adapter class for an event and its updates to be render in the
 * equpdates_layout.xml using the equpdate_item.xml
 *
* */
package com.bbr.attacapp.equpdatesactivity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqContract.EqEntry;
import com.bbr.attacapp.utilitary.Util;


public class EqUpdateAdapter extends CursorAdapter {


    Util util ;
    boolean isUTC = false;
    private final String TAG = "EqUpdateAdapter";
    private Context context;



    public EqUpdateAdapter(Context cxt, Cursor c) {
        super(cxt, c, 0);
        context = cxt;
        util = new Util(context);
        isUTC = util.readPrefSwitch(context, "prefUTCSwitch", "default");
        Log.d(TAG,"Boolean UTC:"+isUTC);
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.equpdate_item, viewGroup, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String dt = "";


        //Magnitude
        TextView tvMagVal = (TextView) view.findViewById(R.id.EqUpMagVal);
        @SuppressLint("Range") float magnitude = cursor.getFloat(cursor.getColumnIndex(EqEntry.MAGNITUDE));
        tvMagVal.setText("Magnitud: "+String.format("%.1f",magnitude));
        if ( magnitude < 4.5 ){
            tvMagVal.setBackgroundColor(Color.GRAY);
        }else if( magnitude >= 4.5 && magnitude < 5.8 ){
            tvMagVal.setBackgroundColor(Color.parseColor("#FFA500"));
        }else{
            tvMagVal.setBackgroundColor(Color.RED);
        }


        TextView tvNearPlace = (TextView) view.findViewById(R.id.EqUpLocationVal);
        tvNearPlace.setSelected(true);
        @SuppressLint("Range") String location = cursor.getString(cursor.getColumnIndex(EqEntry.LOCATION));
        tvNearPlace.setText(location);


        //Depth
        TextView tvDepthVal = (TextView) view.findViewById(R.id.EqUpDepthVal);
        @SuppressLint("Range") float depth = cursor.getFloat(cursor.getColumnIndex(EqEntry.DEPTH));
        tvDepthVal.setText("Profundidad: "+String.format("%.0f",depth)+" km");
        //Log.d(TAG,"ID for this event:"+cursor.getString(cursor.getColumnIndex(EqEntry._ID)));

        //Lat and Lon
        TextView latLon = (TextView) view.findViewById(R.id.EqUpLatLonVal);
        @SuppressLint("Range") float lat = cursor.getFloat(cursor.getColumnIndex(EqEntry.LAT));
        @SuppressLint("Range") float lon = cursor.getFloat(cursor.getColumnIndex(EqEntry.LON));
        latLon.setText("Ubicación, Lat: "+String.format("%.2f",lat)+", Lon: "+String.format("%.2f",lon));

        //sent time
        TextView senttime = (TextView) view.findViewById(R.id.EqUpSentVal);
        @SuppressLint("Range")  long sentTimeUnix = cursor.getLong(cursor.getColumnIndex(EqEntry.SENTTIME));
        if(String.valueOf(sentTimeUnix).length() < 13){
            sentTimeUnix = sentTimeUnix *1000;
        }
        if ( isUTC ){
            dt = util.utcTimestamp2utcISO8601((int) (sentTimeUnix/1000));
            senttime.setText("Enviado: "+dt+" (UTC)");
        }else{
            dt = util.utcTimestamp2localISO8601((int) (sentTimeUnix/1000));
            senttime.setText("Enviado: "+dt+" (Local)");
        }

        //rec time
        TextView rectime = (TextView) view.findViewById(R.id.EqUpRecVal);
        @SuppressLint("Range") long recTimeUnix = cursor.getLong(cursor.getColumnIndex(EqEntry.RECTIME));
        if(String.valueOf(recTimeUnix).length() <13){
            recTimeUnix = recTimeUnix*1000;
        }
        if ( isUTC ){
            dt = util.utcTimestamp2utcISO8601((int) (recTimeUnix/1000));
            rectime.setText("Recibido: "+dt+" (UTC)");
        }else{
            dt = util.utcTimestamp2localISO8601((int) (recTimeUnix/1000));
            rectime.setText("Recibido: "+dt+" (Local)");
        }

        //OriginTime
        TextView tvOrTimeVal = (TextView) view.findViewById(R.id.EqUpDateTimeVal);
        @SuppressLint("Range") int orTimeUnix = cursor.getInt(cursor.getColumnIndex(EqEntry.ORTIME));
        if ( isUTC ){
            dt = util.utcTimestamp2utcISO8601(orTimeUnix);
            tvOrTimeVal.setText("Fecha y Hora : "+dt+" (UTC)");
        }else{
            dt = util.utcTimestamp2localISO8601(orTimeUnix);
            tvOrTimeVal.setText("Fecha y Hora Local: "+dt+ " (Local)");
        }

        //Delay
        TextView delay = (TextView) view.findViewById(R.id.EqUpDelayVal);
        long delayVal = recTimeUnix - sentTimeUnix;
        delay.setText("Delay: "+delayVal/1000.+ " s");
        //delay.setText((eq.getRecTime()-eq.getSentTime())/1000.+" s");

        //now - or time
        TextView likelihood = (TextView) view.findViewById(R.id.EqUpLikeliVal);
        @SuppressLint("Range") float lkh = cursor.getFloat(cursor.getColumnIndex(EqEntry.LKH));
        // String agency = cursor.getString(cursor.getColumnIndex(EqEntry.AGENCY));
        likelihood.setText("Probabilidad: " + lkh);

        //num stations
        TextView numStations = (TextView) view.findViewById(R.id.EqUpNumStaVal);
        @SuppressLint("Range") int numSta = cursor.getInt(cursor.getColumnIndex(EqEntry.NUMARRIVALS));
        numStations.setText("# Estaciones Magnitud: "+numSta);

        //Num Update
        TextView numUpdate = (TextView) view.findViewById(R.id.EqUpNumUpVal);
        @SuppressLint("Range") int num = cursor.getInt(cursor.getColumnIndex(EqEntry.UPDATENO));
        if ( num == 0 ){
            numUpdate.setText("Primera Alerta");
        }else{
            numUpdate.setText("Actualización No: "+num);
        }



    }
}
