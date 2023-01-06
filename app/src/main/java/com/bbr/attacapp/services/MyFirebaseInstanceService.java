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
* This class is for receiving the push notifications from Firebase.
* In addition to this, it notifies with sound/vibration or silently.
* It extends the firebasemessagingservice class,
* Saves info into the local DB and int cloud through Firestore.
*
* */


package com.bbr.attacapp.services;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import androidx.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//import androidx.core.content.LocalBroadcastManager;
import android.util.Log;

//import com.google.firebase.iid.FirebaseInstanceIdService;
import com.bbr.attacapp.alertactivity.Alerting;
import com.bbr.attacapp.eqactivity.EqActivity;
import com.bbr.attacapp.utilitary.EqInfo;
import com.bbr.attacapp.R;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.utilitary.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MyFirebaseInstanceService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseService";
    private String Timestamp_Received= null;
    private String mEvtId;
    private EqDbHelper mEqDbHelper;
    private String evtId ;
    private EqInfo eq;
    private Util util;
    private Boolean evtOnDB = false;
    private int notId = 0;
    private boolean schedule = false;
    private SharedPreferences myPrefs;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String intensityDesc;
    private String intensityColor;
    private boolean myLocation;
    public final static int INTENSITY_THRESHOLD = 4;

    // Notification Categories
    //TODO: Move the below strings to strings.xml
    private String NOTI_CATEGORY_VOICE_ALERT = "Alertas de Voz";
    private String NOTI_CATEGORY_SOUND_ALERT = "Alertas con Sonido";
    private String NOTI_CATEGORY__ALERT_SILENTLY = "Alertas Silenciosas";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"Received new message");

        util = new Util(this);
        //myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //schedule = myPrefs.getBoolean("uploadScheduled", false);
        //int scheduleTime = myPrefs.getInt("lastScheduleTime", 0);
        //int now = util.utcNowTimestamp();
        //int diff = now - scheduleTime;
        //if( diff > 35 && schedule == true){
        //    Log.d(TAG,"Last schedule execution of uploading data is more than 35 seconds and schedule is true");
        //    Log.d(TAG,"setting schedule to false");
        //    schedule = false;
        //}



        // Create the Handler object (on the main thread by default)

        //if ( !schedule ){
        //    Handler handler=new Handler(Looper.getMainLooper());
        //    Runnable r=new Runnable() {
        //        int now  = util.utcNowTimestamp();
        //        public void run() {
        //            new Util.ExportDatabaseCSVTask(getApplicationContext()).execute();
        //            Log.d(TAG,"Scheduled task executed succesfully");
        //            myPrefs.edit().putBoolean("uploadScheduled", false).commit();
//
//
        //        }
        //    };
        //    handler.postDelayed(r, 30000);
        //    Log.d(TAG,"schedule a task to upload data");
        //    schedule = true;
        //    myPrefs.edit().putBoolean("uploadScheduled", true).commit();
        //    myPrefs.edit().putInt("lastScheduleTime", now).commit();
        //}else{
        //    Log.d(TAG,"already scheduled a task");
        //}

        if ( remoteMessage.getData().isEmpty()) {

            //showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            Log.d(TAG,"The notification is EMPTY");
            return;
        }
        else {
            showNotification(remoteMessage.getData());
            Log.d(TAG, "Received a notification: "+remoteMessage.getData().get("body"));
        }



    }

    private void showNotification(Map<String, String> data) {

        String title = data.get("title").toString();
        String body = data.get("body").toString();
        String message = data.get("message").toString();

        Log.d(TAG, "Message title: "+data.get("body"));
        Log.d(TAG, "Message body: "+data.get("message"));

        /*Calling Async task to find out whether this event exists or not*/
        mEqDbHelper = new EqDbHelper(this);
        Util util = new Util(this);
        evtId = message.split(";")[0];

        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = myPrefs.edit();

        editor.putString("lastMsg",message);

        editor.apply();

        try{
            eq = util.alertMsg2eqInfo(message);
        }catch (Exception e){
            Log.e(TAG,"an error has occurred: "+e.toString());
            return;
        }

        //Saving in
        //Intensity estimation
        try{
            String location = eq.getLocation();
            float epicentralDist = Float.parseFloat(location.split(" ")[0]);
            eq.setIntestimated(util.ipe_allen2012_hyp(epicentralDist,eq.getMagnitude(),eq.getDepth()));

        } catch (Exception e) {
            e.printStackTrace();
            eq.setIntestimated(-1);
        }

        //Now and Origin Time for
        //checking if this is an alert or
        //a notification.
        double now = 0;
        try{
            now = util.getUnixTimestampFromTrueTime();
        }catch (Exception e){
            Log.e(TAG,"There was an error getting the truetime time: "+e.toString());
            Log.e(TAG,"Getting from device");
            now = util.utcNowTimestampmsecs();
        }

        int evtOrTime = eq.getOrTime();
        long evtOrTimeMs = (long) (evtOrTime*1000.);
        //Difference Now and Origin time

        double diff;


        int tenMinutes = 60*10*1000;
        Log.d(TAG, evtId);

        //saving in DB
        addUpdateEQ();
        //Intensity estimation
        intensityDesc = "";
        double userLat = Double.longBitsToDouble(myPrefs.getLong("userLat", 0));
        double userLon = Double.longBitsToDouble(myPrefs.getLong("userLon", 0));
        double lastUserLocationTimeMs = myPrefs.getLong("lastUserLocationTimeMs",0);

        int intensity = -1;

        //Difference Now and Origin time
        diff = now - evtOrTimeMs;

        Log.d(TAG,"Now: "+now+", evtOrTimeMs: "+evtOrTimeMs+", evtOrTime: "+evtOrTime);


        if ( diff > tenMinutes ){
            Log.d(TAG,"No further process for notification. Alert msg too old (>10 min)");
            return;
        }

        Log.d(TAG,"Received time minus origin time: "+diff);

        /*checking whether the app is on the foreground or not to send notification*/
        boolean foreground = false;
        try {
            foreground = new ForegroundCheckTask().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if ( foreground ){
            /*No notification*/
            Log.d(TAG,"App on Foreground. There is no notification.");
            return;
        }

        //Checking if this should be notified by magnitude or based on aproximate intensity MM value
        boolean magNotif = util.readPrefSwitchTrue(getApplicationContext(),"prefNotifSwitch","default");
        boolean intAlert = myPrefs.getBoolean("prefAlertsSwitch",true);

        int minInteVal = Integer.valueOf(myPrefs.getString("prefAlertMinIntensity","4"));
        String intDescription = "";
        String intRomanVal = "";

        if ( intAlert ){
            Log.d(TAG, "Alert based on Intensity.");
            Log.d(TAG, "Forcing to magNotif to be false to avoid conflicts.");
            magNotif = false;
        }

        boolean notiSilently = false;

       if ( magNotif ) {
            Log.d(TAG, "Alert based on Magnitude.");
            Log.d(TAG, "Evaluating the magnitude notification thresholds...");
            int prefMaxDepth = util.readPrefIntDepth(getApplicationContext(), "prefMaxDepthNoti", "default");
            float prefMinMag = util.readPrefFloatMag(getApplicationContext(), "prefMinMagNoti", "default");
            String prefAgency = util.readPrefStrAgency(getApplicationContext(), "prefNotiAgency", "default");
            String agency = eq.getAgency().toUpperCase();

            float eqDepth = eq.getDepth();
            float eqMag = eq.getMagnitude();

            if (prefAgency.toUpperCase().equals("ALL")) {
                if (eqMag < prefMinMag || eqDepth > prefMaxDepth) {
                    Log.d(TAG, "No notification for this alert:" + eq.getMagnitude() + ", " + eq.getDepth());
                    Log.i(TAG, "mag:" + eqMag + " " + prefMinMag + " " + eqDepth + " " + prefMaxDepth + " " + prefAgency);
                    notiSilently = true;
                }
            } else {
                if (eqMag <= prefMinMag || eqDepth >= prefMaxDepth || !prefAgency.toUpperCase().equals(agency)) {
                    Log.d(TAG, "No notification for this alert:" + eq.getMagnitude() + ", " + eq.getDepth() + ", " + eq.getAgency());
                    Log.i(TAG, "mag:" + eqMag + " " + prefMinMag + " " + eqDepth + " " + prefMaxDepth + " " + prefAgency);
                    notiSilently = true;
                }
            }
        }

        //Notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

       //Three channels to notify:
        //First one is for alerting with voice message
        String NOTIFICATION_CHANNEL_ID1 = "com.bbr.attacalert1";

        //Second one is for alerting with sound
        String NOTIFICATION_CHANNEL_ID2 = "com.bbr.attacalert2";

        //third one is for notifying silently
        String NOTIFICATION_CHANNEL_ID3 = "com.bbr.attacalert3";

        Uri uri = null;

        NotificationCompat.Builder notificationBuilder = null;

        /*evaluating if this must be notified
        as an alert with voice or sound
        or just notifying with any sound or voice
        */
        boolean alert = false;
        int remainingSeconds= -999;
        /*Strong shake is a flag in case of a defined intensity value is reached
        * If so, then an specific layout will show up to the client when it clicks
        * in the notification*/
        boolean strongShake = false;

        if( intAlert ){
            Log.d(TAG, "Alert based on Intensity. Evaluating...");
            double userLocTimeDiff = now - lastUserLocationTimeMs;

            LatLng epiLatLon = new LatLng( eq.getLat(), eq.getLon() );
            //if the last user location time was more than 15 minutes
            //then it will evaluated on each POI

            LatLng poiLatLonTmp = null;
            String poiNameTmp = null;
            int timeVs = 0;


            if ( userLocTimeDiff < 15*60*1000 ){
                Log.d(TAG,"Using the user's location to evaluate potential alert");
                LatLng userLatLon = new LatLng(userLat, userLon );
                alert = util.alert(userLatLon, epiLatLon, eq.getMagnitude(), eq.getDepth());
                int dist = util.distanceTwoPoints(eq.getLat(), eq.getLon(),userLat, userLon);
                intensity = util.ipe_allen2012_hyp(dist, eq.getMagnitude(), eq.getDepth());
                timeVs = util.travelTimeVs(userLatLon,epiLatLon, eq.getDepth());
                intDescription = "mi Ubicación";
                intRomanVal = util.intensity2RomanDescription(intensity).split(";")[0];
                strongShake = true ? intensity >= util.INTENSITY_STRONGSHAKE_THRESHOLD:false;
            }else{
                Log.d(TAG,"Using the user's poi....");

                List<Object> poiMaxIntensity = util.getMaxIntensityOnPOI(myPrefs,eq);

                if( poiMaxIntensity != null ){
                    //alert = true;
                    poiNameTmp = (String) poiMaxIntensity.get(1);
                    poiLatLonTmp = (LatLng) poiMaxIntensity.get(0);
                    Log.d(TAG,"Poi: "+ poiNameTmp+" is for alerting!");
                    timeVs =  util.travelTimeVs( poiLatLonTmp, epiLatLon, eq.getDepth());
                    intensity = (int) poiMaxIntensity.get(2);
                    if( intensity >= util.INTENSITY_THRESHOLD){
                        alert = true;
                        intDescription = poiNameTmp;
                        intRomanVal = util.intensity2RomanDescription(intensity).split(";")[0];
                        Log.d(TAG,"Description: "+intDescription+", MM: "+intRomanVal);
                        strongShake = true ? intensity >= util.INTENSITY_STRONGSHAKE_THRESHOLD : false;
                    }else{
                        alert = false;
                    }

                }
            }

            if( alert ){
                //Remaining Seconds
                /*
                Based on the user's location or POI or near location, it is estimated
                the arrival of S waves. Ideally, this can be more than zero and that value can be
                used to alert. It can also be negative which might mean the S waves
                have already passed through any of the mentioned places.
                 */
                remainingSeconds = timeVs - (int) (diff/1000);

                if (remainingSeconds < 0){
                    Log.d(TAG, "Seismic S waves might have reached the user's location or POI or near place about "+Math.abs(remainingSeconds)+" seconds ago.");
                }else{
                    Log.d(TAG, "There is "+ remainingSeconds+" seconds to protect yourself!");
                }

            }
        }


        if ( intAlert ){
            Log.d(TAG, "ALERTING BASED ON INTENSITY");
        }
        else if ( magNotif && !notiSilently ) {
            Log.d(TAG, "Notification BASED ON Magnitude");
        }else if( magNotif && notiSilently ){
            Log.d(TAG, "Notifying silently");

        }else{
            Log.d(TAG, "No notification. returning...");
            return;
        }


        NotificationChannel notificationChannel = null;

        //alerting. The user might have some seconds to take some actions.
        if ( (alert && remainingSeconds > 0) || ( !notiSilently && magNotif && diff <=60000 ) ){
            //NOTIFYING WITH VOICE ALERT.
            Log.d(TAG,"Alert sound.");
            uri = Uri.parse("android.resource://"+this.getPackageName()+"/"+ R.raw.notificationsismo);
            notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID1);

            notificationBuilder.setColor(Color.RED);
            notificationBuilder.setColorized(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                try{
                    notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID1);
                }catch (Exception e){
                    Log.e(TAG,"Error deleting a notification for channel ID1. See below...");
                    Log.e(TAG,e.toString());
                }
                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID1,NOTI_CATEGORY_VOICE_ALERT,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setSound(uri, att );
                notificationChannel.setVibrationPattern(new long []{0,1000,500,1000});
            }
        }
        //// remainingSeconds <= 0 seconds. S waves passed or have already passed through a defined place.
        else if ( ( alert && remainingSeconds <= 0 ) || ( !notiSilently && magNotif && diff > 60000 ) ){
            //notify with sound the alert!
            Log.d(TAG,"Notification Sound");
            uri = Uri.parse("android.resource://"+this.getPackageName()+"/"+R.raw.notificationsound);
            notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                try{
                    notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID2);
                }catch (Exception e){
                    Log.e(TAG,"Error deleting a notification for channel ID2. See below...");
                    Log.e(TAG,e.toString());
                }
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID2,NOTI_CATEGORY_SOUND_ALERT,
                        NotificationManager.IMPORTANCE_HIGH);
                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                notificationChannel.setSound(uri, att );
                notificationChannel.setVibrationPattern(new long []{0,1000,500,1000});
            }
        }
        else{
            Log.d(TAG,"Notify the event without a sound or voice. It is not an alert");
            notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID3);
            notificationBuilder.setNotificationSilent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                try{
                    notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID3);
                }catch (Exception e){
                    Log.e(TAG,"Error deleting a notification for channel ID2. See below...");
                    Log.e(TAG,e.toString());
                }
               notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID3,NOTI_CATEGORY__ALERT_SILENTLY,
                        NotificationManager.IMPORTANCE_DEFAULT);
            }
        }


        notificationBuilder.setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSound(uri)
                .setSmallIcon(R.mipmap.ic_attac)
                .setContentTitle("Mag: "+eq.getMagnitude()+", "+eq.getLocation());

        if(alert){
            notificationBuilder.setContentText( intRomanVal+" en "+intDescription+"\nProfundidad: "+eq.getDepth()+" km");
        }else{
            notificationBuilder.setContentText("Profundidad: "+eq.getDepth()+" km");
        }

        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setOnlyAlertOnce(true);

        notificationChannel.setDescription("Notification Channel");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);

        notificationManager.createNotificationChannel((notificationChannel));


        Intent i = null;

        //remainingSeconds > -5 is an arbitrary value. This should be >= 0
        if( alert && strongShake && remainingSeconds > -5 ){
            Log.d(TAG," Alerting a strongshake..." );
            i = new Intent(this, Alerting.class);
            //Changing the alert message!!!
            notificationBuilder.setContentTitle(getString(R.string.dropcoverholdon));
            //notificationBuilder.setContentText();
        }else{
            Log.d(TAG," Not strong shake or it is too late to notify with voice or sound..." );
            i = new Intent(this, EqActivity.class);
        }

        i.putExtra("evtid", evtId);

        int UNIQUE_INT_PER_CALL = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent contentIntent = PendingIntent.getActivity(this, UNIQUE_INT_PER_CALL,
                i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        notificationBuilder.setContentIntent(contentIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //StatusBarNotification[] statusBarNotification = mNotificationManager.getActiveNotifications();
        //for(StatusBarNotification notification: statusBarNotification) {
        //    if (notification.getId() == notId) {
        //        Log.d(TAG, "Found Notification: "+ notId);
        //    }
        //}

        //if(val.isOngoing()){
        //    Log.d(TAG,"Ongoing notification!!");
        //    return;
        //}else{
        //    Log.d(TAG,"NO Ongoing notification!!");
        //}

        mNotificationManager.notify(notId,notificationBuilder.build());

    }

    private void addUpdateEQ() {
        new EqUpdateAddTask(this).execute();

    }

    @Override
    public void onNewToken(String s){
        super.onNewToken(s);
        Log.d("New Token: ",s);

    }


    private class EqUpdateAddTask extends AsyncTask<Void, Void, Cursor> {

        private Context mContext;

        public EqUpdateAddTask (Context context){
            mContext = context;
        }


        @Override
        protected Cursor doInBackground(Void... voids) {
            //getting the evtId
            Cursor cursor = mEqDbHelper.getEqById(evtId);

            if (cursor != null && cursor.moveToLast() ){
                // There is one event
                Log.d(TAG,"There is one event with evtId: "+evtId);
                evtOnDB = true;
            }
            else{
                Log.d(TAG,"There is NO event with evtId: "+evtId);
                evtOnDB = false;
            }
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            /*Broadcast the message in case the app is on foreground*/
            Intent intent = new Intent("com.bbr.attacapp_FCM-MESSAGE");
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);

            if (cursor != null && cursor.moveToLast()) {
                Log.d(TAG,"Event already on local DB.");
                /*Obtaining the tmpEQ object*/
                EqInfo tmpEq = new EqInfo(cursor);
                int update = tmpEq.getUpdate();
                update += 1;
                eq.setUpdate(update);

                notId = tmpEq.getNotid();
                Log.d(TAG,"received not ID:"+notId+" mag: "+tmpEq.getMagnitude() );
                eq.setNotid(notId);

                eq.setLastupdate(util.utcNowTimestamp());

                Log.d(TAG,"Update number: "+eq.getUpdate());

                /*update an event*/
                mEqDbHelper.saveEq(eq);
                intent.putExtra("status","update");

            } else {
                /*Event not found, creating a new one*/
                Log.d(TAG,"Event not found on DB. A new one...");
                /*new event and no updates*/

                eq.setLastupdate((int) (eq.getRecTime()/1000));
                notId = new Random().nextInt(50-1)+1;

                //assuring not having the same ID than the foreground one
                while( notId == 2){
                    notId = new Random().nextInt(50-1)+1;
                }

                Log.d(TAG,"Not ID NEW:"+notId);
                eq.setNotid(notId);
                mEqDbHelper.saveEq(eq);
                intent.putExtra("status","new");

            }

            localBroadcastManager.sendBroadcast(intent);

            //Firestore
            Map<String, Object> datainfo = new HashMap<>();
            datainfo.put("diff", (eq.getRecTime()-eq.getSentTime())/1000.);
            datainfo.put("updateno", eq.getUpdate());
            SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String android_key = myPrefs.getString("deviceid", "NOT_SET");

            if(currentUser != null){
                util.saveOnFirestore( eq.getEvtId(), eq.getUpdate(), android_key, datainfo );
            }else{
                Log.d(TAG,"User not authenticated. Not storing data");
            }

            return;
        }

    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

}
