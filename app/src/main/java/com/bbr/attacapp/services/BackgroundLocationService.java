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
* This actually a foreground location service running
* on a foreground service.
* Background location must be permitted through the user's choice
* at the moment of giving permissions.
* In case the user does not give the permission for background location
* then the service will be started anyway so that there will be a
* foreground service running in order to avoid categorizing the app
* on the restricted or limited apps.
* There is no specific way to avoid the restrictions but this one
* can be one solution temporally. Please, see the documentation
* about background location services and avoiding apps to be restricted by
* the Android OS (from 13 and later versions especially)
*
* */
package com.bbr.attacapp.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bbr.attacapp.R;
import com.bbr.attacapp.mainactivity.MainActivity;
import com.bbr.attacapp.utilitary.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundLocationService extends Service {

    private static final String TAG = "LocationService";
    public static ArrayList<LatLng> locationArrayList = new ArrayList<LatLng>();

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Context context;
    private Timer timer;
    private int NOTIFICATION_ID = 2; //this is the notification ID for the foreground notification.
    private String NOTIFICATION_CHANNEL_ID = "Foreground Notifications";
    private NotificationManager notificationManager;
    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();

        new Notification();
        Util util = new Util(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //location objects
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = util.createLocationRequest();
        locationCallback = util.createLocationCallback(TAG);


        startLocationUpdates();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        //instancing notification manager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // builder
        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSilent(true)//notifying silently
                .setSmallIcon(R.mipmap.ic_attac)
                .setContentTitle("ATTAC Alerta de Terremotos")
                .setContentText("Modo de Recepción de Alertas");
        notificationBuilder.setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            //notification channel is mandatory
            NotificationChannel notificationChannel =new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Foreground Notifications",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notification Channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);

            notificationManager.createNotificationChannel((notificationChannel));

        }

        Intent i = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder.setContentIntent(contentIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            Log.d(TAG,"here");
            startForeground(NOTIFICATION_ID, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);

        }
        else{
            Log.d(TAG,"here?");
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void createNotification(String info){
        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                //.setSound(uri)
                .setSmallIcon(R.mipmap.ic_attac)
                .setContentTitle(getString(R.string.projectTitle))
                .setContentText(info);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSilent(true);
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder.setContentIntent(contentIntent);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //TrueTime
        if(!TrueTime.isInitialized()){
            Log.d(TAG,"TrueTime is not initialized. Init()...");
            init(getApplicationContext());
        }else{
            Log.d(TAG,"TrueTime is already initialized");
        }
        context = getApplicationContext();

        /*TODO: some tasks can be performed here using a timer, handler, or any other
         * to monitor the app performance, checking GPS activity, and more....
         * For now it is just getting the time value from TrueTime and locally
         * to compare and provide the difference in the log information.
        */
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (TrueTime.isInitialized()) {
                    //Getting UTC Time from TrueTime and Device
                    Date trueTime = TrueTime.now();  //TrueTime
                    Date deviceTime = new Date(); //Device
                    long diff = trueTime.getTime() - deviceTime.getTime();
                    Log.d(TAG,"Service Alive. Diff in time between TrueTime and Device time is: "+diff+" ms" );
                }
//
            }
        };
        timer.schedule(task, 0, (60000));// In ms 60 secs is 60000

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void init(final Context context) {
        (new Thread(new Runnable() {
            public void run() {
                try {
                    Log.d("inside","running");
                    TrueTime.build().withNtpHost("time.google.com").withLoggingEnabled(false).
                            withSharedPreferencesCache(context).
                            withConnectionTimeout(31428).initialize();

                } catch (IOException var2) {
                    var2.printStackTrace();
                }
            }
        })).start();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
        Log.d(TAG,"Location updates is started");
    }



}
