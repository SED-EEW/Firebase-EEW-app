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
* Battery optimization might limit the access of the app
* to push notifications (delayed or not delivered at all) since this apps is not used frequently by
* the users.
* Removing the app from the battery optimization might help to avoid the app
* the restriction mentioned previously. Aside from that, removing the app from
* the battery optimization will help to work the app in Doze mode as well as
* battery saving one.
*
* It is up to the user to allow this permission but it is strongly adviced to give it.
*
* */
package com.bbr.attacapp.permissions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bbr.attacapp.services.BackgroundLocationService;
import com.bbr.attacapp.R;
import com.bbr.attacapp.poi.UserPOI;

public class BatteryOptimization extends AppCompatActivity {

    private Button positivePermissionBtn;
    private Button negativePermissionBtn;
    BackgroundLocationService mLocationService = new BackgroundLocationService();
    Intent mServiceIntent;
    private final static String TAG = "BatteryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.batteryoptimization);

        positivePermissionBtn = findViewById(R.id.pospermissionbatterybtn);
        negativePermissionBtn = findViewById(R.id.negpermissionbatterybtn);
        PowerManager pm = (PowerManager) getSystemService(BatteryOptimization.this.POWER_SERVICE);
        final boolean[] isIgnoringBatteryOptimizations = {false};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isIgnoringBatteryOptimizations[0] = pm.isIgnoringBatteryOptimizations(getPackageName());
        }

        if(!isIgnoringBatteryOptimizations[0]){
            Log.d(TAG,"Battery optimization enabled. Waiting for user interaction");
            //showDialogBatteryOptimization();
        }else{
            Log.d(TAG,"Battery optimization disabled. starting a new activity.");
            changeActivity();
        }

        positivePermissionBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d(TAG,"Checking battery optimization");
                    isIgnoringBatteryOptimizations[0] = pm.isIgnoringBatteryOptimizations(getPackageName());
                    if(!isIgnoringBatteryOptimizations[0]){
                        Log.d(TAG,"Battery optimization enabled.");
                        showDialogBatteryOptimization();
                    }else{
                        Log.d(TAG,"Battery optimization disabled.");
                        changeActivity();
                    }
                }else{
                    Log.d(TAG,"Android version lower than "+ Build.VERSION.SDK_INT);
                    changeActivity();
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


    public void showDialogBatteryOptimization() {
        Log.d(TAG,"Asking Optimization Battery...");
        Intent i  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        i.setData(Uri.parse("package:"+getPackageName()));
        i.putExtra("requestCode","1");
        //startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+getPackageName())));
        activityResultLaunch.launch(i);

    }
    public void changeActivity(){

        Intent i = new Intent(BatteryOptimization.this, UserPOI.class);
        Log.d(TAG,"starting a new activity: UserPOI");
        startActivity(i);
        finish();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG,"Result code positive");
                    }else{
                        Log.d(TAG,"Result code negative");
                    }
                    changeActivity();
                }
            });


}
