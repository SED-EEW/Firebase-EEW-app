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
* This class is for adaption information of an EQ
* in the window information of each marker on the map.
*
* */
package com.bbr.attacapp.mapactivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.bbr.attacapp.R;
import com.bbr.attacapp.mapactivity.InfoWindowData;
import com.bbr.attacapp.utilitary.Util;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private String TAG = "CustomWindowAdapter";
    private boolean isLocEnabled;
    private Util util;
    public CustomWindowAdapter(Context ctx, boolean locEnabled){
        context = ctx;
        Log.d(TAG,"Starting CustomWindowsAdapter...");
        isLocEnabled = locEnabled;
        util =  new Util(context);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.infowindow_layout, null);

        TextView mag = view.findViewById(R.id.iw_mag);
        TextView nearplace = view.findViewById(R.id.iw_nearplace);

        TextView datetime = view.findViewById(R.id.iw_datetime);
        TextView depth = view.findViewById(R.id.iw_depth);
        TextView timespan = view.findViewById(R.id.iw_timespan);
        TextView agency = view.findViewById(R.id.iw_agency);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        try{
            mag.setText(infoWindowData.getMagnitude());
        }catch (Exception e){
            Log.i(TAG,"it is user location");
            return null;
        }

        float magVal = Float.valueOf(infoWindowData.getMagnitude());
        if ( magVal < 4.5 ){
            mag.setBackgroundColor(Color.GRAY);
        }else if( magVal >= 4.5 && magVal < 5.8 ){
            mag.setBackgroundColor(Color.parseColor("#FFA500"));
        }else{
            mag.setBackgroundColor(Color.RED);
        }
        nearplace.setText(infoWindowData.getNearplace());

        datetime.setText(infoWindowData.getDatetime());

        depth.setText(infoWindowData.getDepth());

        timespan.setText(infoWindowData.getTimespan());

        agency.setText(infoWindowData.getAgency());

        Log.d(TAG,mag+" "+nearplace);


        TextView intTV = (TextView) view.findViewById(R.id.iw_intensity);

        String intDesc = "--";
        String intColor = "#FFFFFF";
        int intTextColor = Color.GRAY;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        float epilat = Float.valueOf(infoWindowData.getEpiLat());
        float epilon = Float.valueOf(infoWindowData.getEpiLon());
        float magnitude = Float.valueOf(infoWindowData.getMagnitude());
        float depthVal = Float.valueOf(infoWindowData.getDepthVal());
        int nearPlaceDistance = Integer.valueOf(infoWindowData.getNearPlaceDist());
        String evtId = infoWindowData.getEvtId();

        List<Object> arrIntensity = util.getIntensityDescAndColor(sharedPreferences, epilat, epilon,
                depthVal, magnitude, nearPlaceDistance,evtId, isLocEnabled);



        String type = (String) arrIntensity.get(0);
        intDesc = (String) arrIntensity.get(1);
        intColor = (String) arrIntensity.get(2);
        intTextColor = (int) arrIntensity.get(3);

        intTV.setBackgroundColor(Color.parseColor(intColor));
        intTV.setTextColor(intTextColor);



        if(type.equals("reported")){
            intTV.setText(intDesc+"\n(Reportada)");
        }else{
            if(type.equals("myLocation")){

                if( intDesc.equals("--") ) {
                    intTV.setText("Sin Intensidad\n(en su ubicación)");
                }else{
                    intTV.setText(intDesc + "\n(Estimado en su ubicación)");
                }

            }else if( type.equals("nearLocation")){

                intTV.setText(intDesc + "\n(Estimado al lugar más cercano)");

            }else{
                intTV.setText(intDesc + "\n(estimado en "+type+")");

            }

        }


        return view;
    }
}
