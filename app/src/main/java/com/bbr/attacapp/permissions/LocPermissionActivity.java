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
* This is a controller for location permission layout.
* It asks the permission for Coarse location and Background Location.
* If they are granted, then the foreground service is started.
*
*
* */
package com.bbr.attacapp.permissions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bbr.attacapp.services.BackgroundLocationService;
import com.bbr.attacapp.R;
import com.bbr.attacapp.utilitary.Util;

public class LocPermissionActivity extends AppCompatActivity {

    private Button positivePermissionBtn;
    private Button negativePermissionBtn;
    BackgroundLocationService mLocationService = new BackgroundLocationService();
    Intent mServiceIntent;
    private final static String TAG = "LocationPermissionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissionlocation);

        positivePermissionBtn = findViewById(R.id.positivepermissionbtn);
        negativePermissionBtn = findViewById(R.id.negativepermissionbtn);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                changeActivity();
            }
        }

        positivePermissionBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(LocPermissionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        if (ActivityCompat.checkSelfPermission(LocPermissionActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG,"Permission NOT granted for background location");
                            requestBackgroundLocationPermission();


                        }else if (ActivityCompat.checkSelfPermission(LocPermissionActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                == PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG,"Starting service");
                            startServiceFunc();
                            changeActivity();
                        }
                    }else{
                        Log.d(TAG,"Starting service");
                        startServiceFunc();
                        changeActivity();
                    }

                }else if (ActivityCompat.checkSelfPermission(LocPermissionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(LocPermissionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION )) {

                        requestCoarseLocationPermission();


                    } else {
                        requestCoarseLocationPermission();
                    }
                }

            }
        });
        negativePermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeActivity();
            }
        });
    }


    private void startServiceFunc(){
        mLocationService = new BackgroundLocationService();
        mServiceIntent = new Intent(this, mLocationService.getClass());

        //startService(new Intent(this,NewService.class));
        if (!Util.isMyServiceRunning(mLocationService.getClass(), this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startService(mServiceIntent);
            }
            Log.d(TAG,"Service started");
        } else {
            Log.d(TAG, "Service already running");
        }
    }

    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                2);
    }

    private void requestCoarseLocationPermission() {
        ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == 1){

            if (grantResults.length !=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission();
                }

            } else {
                Log.d(TAG,"ACCESS_FINE_LOCATION permission denied");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                 /*   startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", this.getPackageName(), null),),);*/

                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:"+getPackageName())
                    ));

                }
            }
            return;

        }else if (requestCode == 2){

            if (grantResults.length!=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Background location Permission Granted");
                    Log.d(TAG,"Starting service after background permission granted");
                    startServiceFunc();
                }
            } else {
                Log.d(TAG, "Background location permission denied");
                Log.d(TAG,"Starting service after background permission was not granted");
                startServiceFunc();

            }
            changeActivity();
            return;
        }

    }
    public void changeActivity(){

        Intent i = new Intent(LocPermissionActivity.this, BatteryOptimization.class);
        startActivity(i);
        Log.d(TAG,"starting a new activity: Battery Optimization");
        finish();
    }

}

