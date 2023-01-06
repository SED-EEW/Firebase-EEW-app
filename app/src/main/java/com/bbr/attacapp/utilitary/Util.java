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
 * This class contains some methods for utilitary purpose used on
 * the mayority of classes of this project.
 * It needs the context from this class is instanced.
 *
 */
package com.bbr.attacapp.utilitary;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import androidx.preference.PreferenceManager;
//import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bbr.attacapp.services.BackgroundLocationService;
import com.bbr.attacapp.csv.CSVWriter;
import com.bbr.attacapp.dbsqlite.EqDbHelper;
import com.bbr.attacapp.mainactivity.MainActivity;
import com.bbr.attacapp.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.text.*;

import com.google.android.gms.location.LocationRequest;
import com.instacart.library.truetime.TrueTime;

public class Util {

    private final String TAG = "Util";
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
    private final String APP_VERSION = "0.5.0";
    private SharedPreferences sharedPreferences;
    private Context context;
    Location location;
    public final static int INTENSITY_THRESHOLD = 4;
    public final static int INTENSITY_STRONGSHAKE_THRESHOLD = 6;
    private final float VS_VELOCITY = 3.5f;

    /*////////////////////////////////
        set the below value based on the POIs on UserPOI Class
        /////////////////////////////////*/
    private final int MAX_POI_NUMBERS = 4;


    // Added a constructor for obtaining the context.
    // this is used in some methods
    public Util(Context cxt){
        context = cxt;
    }

    private boolean createdCsv = false;

    /*Method to subscribe to Firebase Push Notification to a specific topic*/
    public void FCMSubscribeTopic(String topic){
        Boolean tmp;
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to topic: "+topic;
                        if (!task.isSuccessful()) {
                            msg = "Not Subscribed";
                        }

                        Log.d(TAG, msg);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                    }
                });
        //This will contain a list of topics that are used
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("alerttest");
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("attactest");
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("inetertest");


    }

    /*Method to subscribe to Firebase Push Notification to a specific topic*/
    public void FCMUnsubscribeTopic(String topic){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }


    /*Public method to convert unix timestamp in int type to
    * string in ISO8601 format without milliseconds*/
    public String utcTimestamp2localISO8601( int timestamp ){
        //Unix seconds
        if (timestamp < 0){
            return null;
        }
        //convert seconds to milliseconds
        Date date = new Date(timestamp*1000L);
        // format of the date
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //jdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
        jdf.setTimeZone(TimeZone.getDefault());
        String dt = jdf.format(date);
        //System.out.println("\n"+java_date+"\n");
        return dt;
    }

    /*Public method to convert unix timestamp (no milliseconds) in String in ISO8601 format without the milliseconds*/
    public String utcTimestamp2utcISO8601( int timestamp ){
        //Unix seconds
        if (timestamp < 0){
            return null;
        }
        //convert seconds to milliseconds
        Date date = new Date(timestamp*1000L);
        // format of the date
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //jdf.setTimeZone(TimeZone.getDefault());
        String dt = jdf.format(date);
        return dt;
    }


    public String nowLocalDtISO8601(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        String dt = sdf.format(date);
        return dt;

    }

    /*now local time in timestamp to ISO8601*/
    public String nowLocalToTimeISO8601(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        String dt = sdf.format(date);
        return dt;

    }


    /*now utc time in timestamp to ISO8601*/
    public String nowUtcDtISO8601(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dt = sdf.format(date);
        return dt;

    }

    /*now utc time in timestamp to ISO8601 only HOUR AND MIN*/
    public String nowUtcToTimeISO8601(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dt = sdf.format(date);
        return dt;

    }

    /* UTC now timestamp not milliseconds*/
    public int utcNowTimestamp(){
        double tmp = System.currentTimeMillis();
        int timestamp = (int) ( tmp/1000. );
        return timestamp;
    }
    public long utcNowTimestampmsecs(){
        long tmp = System.currentTimeMillis();
        return tmp;
    }

    // This method is to parse the notification
    // that comes as a push notification.
    // It returns an EqInfo object.
    public EqInfo alertMsg2eqInfo(String msg){
        String[] eqArr = msg.split(";");
        long recTime = 0;
        if (eqArr[8].toLowerCase().equals("null")){
            /*Received time is null*/
            //getting the current time

            long now = getUnixTimestampFromNTP();
            if (now == 0){
                now = utcNowTimestampmsecs();
            }
            recTime =  now;
        }else{
            recTime = Long.valueOf(eqArr[8]);
        }
        EqInfo eqInfo = null;

        if( eqArr.length == 18 ){
             eqInfo = new EqInfo(eqArr[0],Float.valueOf(eqArr[1]),Float.valueOf(eqArr[2]),
                    Float.valueOf(eqArr[3]),Float.valueOf(eqArr[4]),Float.valueOf(eqArr[5]),Integer.valueOf(eqArr[6]),Long.valueOf(eqArr[16]),
                    recTime,eqArr[9],eqArr[10],eqArr[11],eqArr[12],eqArr[13],eqArr[14],Integer.valueOf(eqArr[15]),Integer.valueOf(eqArr[17]));

        }else{//remove this!
            eqInfo = new EqInfo(eqArr[0],Float.valueOf(eqArr[1]),Float.valueOf(eqArr[2]),
                    Float.valueOf(eqArr[3]),Float.valueOf(eqArr[4]),Float.valueOf(eqArr[5]),Integer.valueOf(eqArr[6]),Long.valueOf(eqArr[16]),
                    recTime,eqArr[9],eqArr[10],eqArr[11],eqArr[12],eqArr[13],eqArr[14],Integer.valueOf(eqArr[15]),20);
        }

        return eqInfo;
    }


    /* using the Origin Time  (int type) obtains the
    * time span from that moment to now
    * in a format:
    * XX days, XX hours, XX minutes, XX seconds
    * in Spanish )_(
    * */

    public String getTimeSpanFromNow(int orTime){
        int now = utcNowTimestamp();
        int diff = now - orTime;
        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        int days = 0;
        int res = 0;
        String timeSpan = "";

        days = diff / 86400;
        res = diff % 86400;
        hours = res / 3600;
        res = res % 3600;
        minutes = res / 60;
        res = res % 60;
        seconds = res;

        timeSpan +=  (days > 0)? String.valueOf(days) + " días, " : "";
        timeSpan += ( hours > 0 ) ? String.valueOf(hours) + " horas, " : "";
        timeSpan += ( minutes > 0 && days == 0 ) ? String.valueOf(minutes) + " minutos, " : "";
        timeSpan += ( seconds > 0 && days == 0 && hours == 0 ) ? String.valueOf(seconds) + " segundos" : "";
        /*TODO: Remove the last , value*/

        if (diff<=0){
            timeSpan = "--";
        }else{
            if (timeSpan.substring(timeSpan.length()-2,timeSpan.length()).equals(", ")) {
                timeSpan = timeSpan.substring(0, timeSpan.length() - 2);
            }
        }
        return timeSpan;
    }

    /*
    * Returns the difference in seconds from the origin time to NOW!
    * */
    public int getTimespanFromNowSeconds(int ortime){
        int now =utcNowTimestamp();
        int diff = now - ortime;
        return diff;
    }

    /*to set user's preference values
    * it needs the contexts from the activity or fragment where this is called*/
    public void setPreferences(Context context, String prefTitle, String prefValue, String prefType){
        SharedPreferences myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        Log.d(TAG,"Setting: "+prefTitle+"="+prefValue);
        if (prefType == "boolean"){
            editor.putBoolean(prefTitle, Boolean.valueOf(prefValue));
        }else if(prefType == "String"){
            editor.putString(prefTitle, prefValue);
        }else if(prefType == "int"){
            editor.putInt(prefTitle, Integer.valueOf(prefValue));
        }else if(prefType == "float"){
            editor.putFloat(prefTitle, Float.valueOf(prefValue));
        }else if(prefType == "double" || prefType == "long" ){
            editor.putLong(prefTitle, Long.valueOf(prefValue));
        }

        editor.apply();
        return;
    }

    /*to read user's preference values
     * it needs the contexts from the activity or fragment where this is called*/
    public String readPreferences(Context context, String prefTitle, String prefType, String typePref){
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }

        String val = null;

        if (myPrefs == null){
            return "";
        }

        try{
            if (prefType == "boolean"){
                val = String.valueOf(myPrefs.getBoolean(prefTitle,false ));
                Log.d(TAG,"Returning:"+prefTitle+"="+val);
            }else if(prefType == "String"){
                val= String.valueOf(myPrefs.getString(prefTitle, ""));
                Log.d(TAG,"Returning:"+prefTitle+"="+val);
            }else if(prefType == "int"){
                val = String.valueOf( myPrefs.getInt(prefTitle, 0));
                Log.d(TAG,"Returning:"+prefTitle+"="+val);
            }else if(prefType == "float"){
                val = String.valueOf(myPrefs.getFloat(prefTitle, 0.0f ));
                Log.d(TAG,"Returning:"+prefTitle+"="+val);
            }else if(prefType == "double" || prefType == "long" ){
                val = String.valueOf(myPrefs.getLong(prefTitle, 0 ));
                Log.d(TAG,"Returning:"+prefTitle+"="+val);
            }

        }catch (Exception e){
            Log.e(TAG,"Not possible to obtain: "+prefTitle);
            Log.e(TAG,e.toString());
            val = null;
        }
        return val;
    }


    /*
    *
    * PREFERENCE VALUES SET BY USERS THROUGH SETTINGS ACTIVITY
    *
    * */
    public int readPrefIntDepth(Context context, String prefkey, String typePref) {
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }
        int val = Integer.valueOf(myPrefs.getString(prefkey,"800"));
        return val;
    }

    /* To read the user's min mag floating value set on preferences*/
    public float readPrefFloatMag(Context context, String prefkey, String typePref) {
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }
        float val = Float.valueOf(myPrefs.getString(prefkey,"0.0f"));
        return val;
    }

    public String readPrefStrAgency(Context context, String prefkey, String typePref) {
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }
        String val = myPrefs.getString(prefkey,"all");
        return val;
    }
    public Boolean readPrefSwitch(Context context, String prefkey, String typePref) {
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }
        Boolean val = myPrefs.getBoolean(prefkey,false);
        return val;
    }
    public Boolean readPrefSwitchTrue(Context context, String prefkey, String typePref) {
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }
        boolean val = myPrefs.getBoolean(prefkey,false);
        return val;
    }
    public int readPrefIntDaysAgo(Context context, String prefkey, String typePref) {
        SharedPreferences myPrefs = null;
        if (typePref.equals("default")){
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            //myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else if( typePref.equals("configuration")){
            myPrefs = context.getSharedPreferences("ConfigOptions", Context.MODE_PRIVATE);
        }else{
            //NO
        }
        int val = Integer.valueOf(myPrefs.getString(prefkey,"30"));
        return val;
    }
         /* END OF USER PREFERENCE VALUES*/
        //////////////////////////////////

    // RANDOM STRINGS is to generate a random alphanumeric string
    // with the defined len stablished in the input variable
    public String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    //this method is to create a string that identies a device.
    public  void createVerifierStrings(Context context) {

        String not_set = "NOTSET";
        String android_key;
        SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        android_key = myPrefs.getString("deviceid", not_set);
        Log.d(TAG,android_key+" key");

        if (android_key.equals(not_set)) {
            Log.d(TAG, "Creating keys for 1st time");
            android_key = this.randomString(5);
            myPrefs.edit().putString("deviceid", android_key).commit();
            Log.d(TAG, "The ID is: "+android_key);

        }
    }


    /*An async task to export data from user device DB to CSV file*/
    public static class ExportDatabaseCSVTask extends AsyncTask<Context, Void, Boolean> {
        //private final ProgressDialog dialog ;
        SharedPreferences myPrefs ;
        private String deviceid;
        private File exportDir;
        private Context context;
        File file;
        String data = "";
        public ExportDatabaseCSVTask(Context context){
           // dialog = new ProgressDialog(context);
            this.context = context;
        }


        @Override
        protected void onPreExecute() {
            myPrefs =  PreferenceManager.getDefaultSharedPreferences(context);
            deviceid = myPrefs.getString("deviceid", Build.MANUFACTURER+Build.MODEL);
            //dialog.setMessage("Exportando datos...");
            //dialog.show();
        }
        @Override
        protected Boolean doInBackground(Context... contexts) {
            exportDir = new File(Environment.getExternalStorageDirectory(), "");
            Log.d("util AsyncTask","Here..." +exportDir.toString());
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            Log.d("util AsyncTask",Build.MANUFACTURER+" and "+Build.MODEL);

            file = new File(exportDir, deviceid);
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                EqDbHelper mEqDbHelper =  new EqDbHelper(context);
                int lastUpdate = myPrefs.getInt("lastSentData",0);
                Log.d("Util ASyncTask","Last uploaded time is: "+lastUpdate);
                //lastUpdate = 1653350400 ;

                Cursor curCSV;

                //deleting Mensaje de Prueba test events
                int tmp = mEqDbHelper.deleteTestEqs("Mensaje de Prueba");

                if (lastUpdate == 0 ){
                    Log.d("Util data","collecting full data");
                    curCSV = mEqDbHelper.getAllEqsList();
                }else{
                    curCSV = mEqDbHelper.getDataFromUnixtimeEqs(lastUpdate);
                }

              //  csvWrite.writeNext(curCSV.getColumnNames());

                while (curCSV.moveToNext()) {
                    data += curCSV.getString(0)+","+curCSV.getString(1)+","+ curCSV.getString(2)+","+curCSV.getString(3)+","+curCSV.getString(4)+","+curCSV.getString(5)+","+curCSV.getString(6)+","+curCSV.getString(7)+","+
                            curCSV.getString(8)+","+curCSV.getString(9)+","+curCSV.getString(10)+","+curCSV.getString(11)+","+curCSV.getString(12)+","+
                            curCSV.getString(13)+","+curCSV.getString(14)+","+curCSV.getString(15)+","+curCSV.getString(16)+","+curCSV.getString(17)+","+curCSV.getString(18);
                    String arrStr[] = {curCSV.getString(1), curCSV.getString(2),
                                               curCSV.getString(3),curCSV.getString(4),curCSV.getString(5),curCSV.getString(6),curCSV.getString(7),
                                              curCSV.getString(8),curCSV.getString(9),curCSV.getString(10),curCSV.getString(11),curCSV.getString(12),
                                             curCSV.getString(13),curCSV.getString(14),curCSV.getString(15),curCSV.getString(16),curCSV.getString(17),curCSV.getString(18),curCSV.getString(19)};
                     //curCSV.getString(3),curCSV.getString(4)//};
                    csvWrite.writeNext(arrStr);
                    data +="\n";
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                Log.e("util AsyncTask", e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            /*if (dialog.isShowing()) {
                dialog.dismiss();
            }*/
            if (success) {

                //Toast.makeText(context, "Datos exportados y listos para Enviar", Toast.LENGTH_SHORT).show();
                //this.sendData(exportDir.toString()+"/"+deviceid+".csv");
                //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                //StrictMode.setVmPolicy(builder.build());
                //Intent i = Util.sendMail(context, "billyburgoa@gmail.com","ID:"+deviceid, data,exportDir.toString()+"/"+deviceid+".csv");
                //context.startActivity(Intent.createChooser(i, "E-mail"));
                if ( file.length() == 0 ){
                    Log.d("util async","File is empty. Not uploading anything.");
                }
                else{
                    Log.d("util async","File contains this amount of lines: "+file.length());
                    new UploadFileAsync(context).execute();
                }



            } else {
                //Toast.makeText(context, "Falló la exportación!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    /**/
    public void displayAlertDialogAbout(Context context, String title, String body) {
        String message = context.getString(R.string.aboutapp);
      //String message = "<h2>ATTAC Alerta Temprana de Terremotos.</h2>\n" +
      //           "<p style=\"text-align: justify;\">Alerta Temprana de Terremotos en América Central (ATTAC) es un proyecto conjunto de los Centros Sismológicos de:</p>\n" +
      //        "<ul style=\"text-align: justify;\">\n" +
      //        "<li>INETER, Nicaragua.</li>\n" +
      //        "<li>OVSICORI-UNA, Costa Rica.</li>\n" +
      //        "<li>MARN, El Salvador.</li>\n" +
      //        "<li>INSIVUMEH, Guatemala.</li>\n" +
      //        "<li>Swiss Seismological Service, Suiza.</li>\n" +
      //        "</ul>\n" +
      //        "<p style=\"text-align: justify;\">Esta Aplicación Móvil tiene como fin presentar las alertas emitidas por estos centros sismológicos en una primera etapa de PRUEBA.</p>\n" +
      //        "<p style=\"text-align: justify;\">La información presentada podría contener errores o tener algún retrazo en ser recibida o definitivamente no ser recibida por la App.</p>\n" +
      //        "<p style=\"text-align: justify;\">Durante la etapa de prueba la app enviará datos relacionados a tiempos de recepción de las alertas y otros parámetros para conocer el funcionamiento de la misma y se puedan hacer mejoras. No se enviará ningún dato personal (imagen ni texto) o que no esté relacionado con la app y su contexto.</p>\n" +
      //        "<p style=\"text-align: justify;\">Para reportar la Intensidad (lo que sintió y/o vio) se le pedirá compartir su ubicación. Importante indicar que la ubicacion que se usa es la <b>aproximada</b> y no es la precisa.</p>\n"+
      //        "<p style=\"text-align: justify;\">Créditos a \"European-Mediterranean Seismological Centre\" (EMSC) por los Pictogramas que describen el valor de Intensidad.</p>\n"+
      //        "<p style=\"text-align: justify;\">Consultas y sugerencias a billyburgoa@gmail.com</p>\n"+
      //        "<p style=\"text-align: left;\"> Versión: <strong>"+APP_VERSION+"</strong></p>" ;
        String button1String = "Ok";
        String button2String = "No";

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        //ad.setTitle(title);
        //ad.setMessage(R.string.aboutapp);
        ad.setMessage(Html.fromHtml(message,Html.FROM_HTML_MODE_LEGACY));

        ad.setPositiveButton(
                button1String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                       // new Util.ExportDatabaseCSVTask(context).execute();
                    }
                }
        );


        //
        ad.show();
    }

    /*Async task to upload a file created by ExportDatabaseCSV method.*/

    private static class UploadFileAsync extends AsyncTask<String, Void, String> {
        private Context context;
        private String LOCALTAG = "Upload file";
        public UploadFileAsync(Context context){
            //dialog = new ProgressDialog(context);
            this.context = context;
        }
        @Override
        protected String doInBackground(String... params) {

            try {
                File exportDir = new File(Environment.getExternalStorageDirectory(), "");

                SharedPreferences mPref =  PreferenceManager.getDefaultSharedPreferences(context);
                String deviceid = mPref.getString("deviceid", Build.MANUFACTURER+Build.MODEL);
                File file = new File(exportDir, deviceid);

                String sourceFileUri = file.toString();

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(sourceFileUri);

                if (sourceFile.isFile()) {

                    try {
                        Log.d(LOCALTAG,"Starting a request to upload csv information...");
                        String upLoadServerUri = "http://165.98.224.45/savedata.php?";

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(
                                sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // Open a HTTP connection to the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE",
                                "multipart/form-data");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("file", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        String tmp = "Content-Disposition: form-data; name=\"file\";filename=\"";
                        dos.writeBytes(tmp + sourceFileUri + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math
                                    .min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0,
                                    bufferSize);

                        }

                        // send multipart form data necesssary after file
                        // data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens
                                + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn
                                .getResponseMessage();

                        if (serverResponseCode == 200) {

                            // messageText.setText(msg);
                            //Toast.makeText(ctx, "File Upload Complete.",
                            //      Toast.LENGTH_SHORT).show();

                            // recursiveDelete(mDirectory1);
                            Log.d(LOCALTAG, "all went fine");
                            Log.d(LOCALTAG,"Response:"+serverResponseMessage);
                            double dt = System.currentTimeMillis();
                            int timestamp = (int) ( dt/1000. );
                            mPref.edit().putInt("lastSentData", timestamp).commit();

                        }else{
                            Log.d(LOCALTAG, "something failed");
                            Log.d(LOCALTAG,"Response:"+serverResponseMessage);
                            Toast.makeText(context, "Hubo un problema. Por favor, intente más tarde.", Toast.LENGTH_LONG).show();

                        }

                        // close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {

                        // dialog.dismiss();
                        e.printStackTrace();

                    }
                    // dialog.dismiss();

                } // End else block


            } catch (Exception ex) {
                // dialog.dismiss();

                ex.printStackTrace();
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    /*Intensity value from epicentral distance, magnitude and depth values.
    * This was
    * */
    public int ipe_allen2012_hyp(float epiDistance, float magnitude, float depth){//hypoDistance km
        double a = 2.085;
        double b = 1.428;
        double c = -1.402;
        double d = 0.078;
        double s = 1.0;
        double m1=-0.209;
        double m2=2.042;

        if ( depth<0 ){
            return -1;
        }

        //Obtaining the hypocentral distance
        //The form is a triangle - Can be improved(?)
        double hypoDistance = Math.sqrt(Math.pow(epiDistance,2)+Math.pow(depth,2));

        double rm = m1+m2*Math.exp(magnitude-5);
        double I = a + b*magnitude + c*Math.log(Math.sqrt(Math.pow(hypoDistance,2) + Math.pow(rm,2)))+s;
        if (hypoDistance < 50 ){
            I = a + b*magnitude + c*Math.log(Math.sqrt(Math.pow(hypoDistance,2) + Math.pow(rm,2)))+d*Math.log(hypoDistance/50)+s;
        }

        return (int) Math.round(I); // intensity (MMI, float)

    }

    /*Gettng the Roman Description from intensity value.
    * This goes from -1 which is nothing (--) to 12 which is
    * catastrophic (XII).
    * It also returns the color that belongs to a defined intensity value.
    * When this method is called then returned value is a string that needs to be
    * splitted using the regexp ;
    *
    * */

    public String intensity2RomanDescription(int intensity){
        //TODO: Implement this in Strings.xml
        Hashtable<Integer, String> my_dict = new Hashtable<Integer, String>();
        Log.d(TAG,"Intensity Input:"+ intensity);
        my_dict.put(-1, "--;#FFFFFF");
        my_dict.put(0, "--;#D3D3D3");
        my_dict.put(1, "I. No Sentido;#FFFFFF");
        my_dict.put(2, "II. Muy Débil;#BFCCFF");
        my_dict.put(3, "III. Leve;#9999FF");
        my_dict.put(4, "IV. Moderado;#80FFFF");
        my_dict.put(5, "V. Poco Fuerte;#7DF894");
        my_dict.put(6, "VI. Fuerte;#FFFF00");
        my_dict.put(7, "VII. Muy Fuerte;#FFC800");
        my_dict.put(8, "VIII. Destructivo;#FF9100");
        my_dict.put(9, "IX. Muy Destructivo;#FF0000");
        my_dict.put(10, "X. Desastroso;#C80000");
        my_dict.put(11, "XI. Muy Desastroso;#800000");
        my_dict.put(12, "XII. Catastrófico;#000000");
        Log.d(TAG,"Intensity description and color:"+ my_dict.get(intensity));
        if(intensity<0){
            return my_dict.get(-1);
        }else{
            if ( intensity > 12 ){
                //in case of something crazy happens, then
                //the maximum value is returned
                return  my_dict.get(12);
            } else {
                return  my_dict.get(intensity);
            }

        }
    }

    /*FIRESTORE METHOD to store the information into a remote non-SQLDB  */
    public boolean saveOnFirestore(String eventid,int updateno, String usercode, Map<String, Object> datainfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try{

            DocumentReference df = db.collection("eqalerts").document(eventid);

            Map<String, Object> initialData = new HashMap<>();

            Map<String, Object> updateObj = new HashMap<>();

            updateObj.put(String.valueOf(updateno),datainfo);

            initialData.put(usercode,updateObj);

            df.set( initialData, SetOptions.merge() );

            return true;

        }catch ( Exception e ){
            Log.e(TAG,"There was an error saving information on Firestore. See below...");
            Log.e(TAG,e.toString());
            return false;
        }

    }

    /* it gets the azimuth from the nearplace to the epicenter.*/
    public String findazimuth(double poiLat,double poiLon,double epiLat,double epiLon){
        double rlat1=Math.toRadians(poiLat);
        double rlon1=Math.toRadians(poiLon);
        double rlat2=Math.toRadians(epiLat);
        double rlon2=Math.toRadians(epiLon);
        double az = Math.toDegrees(Math.atan2(Math.sin(rlon2-rlon1),Math.cos(rlat1)*Math.tan(rlat2)-Math.sin(rlat1)*Math.cos(rlon2-rlon1)));
        if ( az<0 ){
            az=az+360;
        }

        if ( az>0 && az<=10){
            return "Norte";
        }
        if ( az>10 && az<=20){
            return "Norte Noreste";
        }
        if ( az>20 && az<=70){
            return "Noreste";
        }
        if ( az>70 && az<=80){
            return "Este Noreste";
        }
        if ( az>80 && az<=100){
            return "Este";
        }
        if ( az>100 && az<=110){
            return "Este Sureste";
        }
        if ( az>110 && az<=160){
            return "Sureste";
        }
        if ( az>160 && az<=190){
            return "Sur";
        }
        if ( az>190 && az<=200){
            return "Sur Suroeste";
        }
        if ( az>200 && az<=250){
            return "Suroeste";
        }
        if ( az>250 && az<=260){
            return "Oeste Suroeste";
        }
        if ( az>260 && az<=280){
            return "Oeste";
        }
        if ( az>280 && az<=290){
            return "Oeste Noroeste";
        }
        if ( az>290 && az<=340){
            return "Noroeste";
        }
        if ( az>340 && az<=350){
            return "Norte Noroeste";
        }
        if ( az>350 && az<=360){
            return "Norte";
        }
        return "";
    }

    /*Distance between two points. This is generally from
    * the epicenter to some place (near location, user's location, etc)*/
    public int distanceTwoPoints(double epiLat, double epiLon, double lat, double lon){
        double rEpiLat = Math.toRadians(epiLat);
        double rEpiLon = Math.toRadians(epiLon);
        double rlat = Math.toRadians(lat);
        double rlon = Math.toRadians(lon);

        double distance=Math.acos(Math.sin(rEpiLat)*Math.sin(rlat)+Math.cos(rEpiLat)*Math.cos(rlat)*Math.cos(rEpiLon-rlon))*6371;

        return (int) distance;
    }


    /*This method is to get microseconds timestamp from a NTP server*/
    public long getUnixTimestampFromNTP(){
        String NTPserverURL = "time.google.com";
        int millisecondsTimeout = 500;
        long nowAsPerDeviceTimeZone = 0;

        SntpClient sntpClient = new SntpClient();
        if (sntpClient.requestTime(NTPserverURL, millisecondsTimeout)) {
            nowAsPerDeviceTimeZone = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
        }

        return nowAsPerDeviceTimeZone;
    }

    /*
    * Location Request - instanced object to be returned
    * */
    public LocationRequest createLocationRequest() {
        LocationRequest locationRequest;
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(15*60*1000);
        locationRequest.setFastestInterval(5*60*000);
        locationRequest.setMaxWaitTime(18*60*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        return locationRequest;
    }

    /*
     * LocationCallback - instanced object returned
     */

    public LocationCallback createLocationCallback(String tag){
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location =  locationResult.getLastLocation();
                Log.d(tag,"Lat: "+ Double.toString(location.getLatitude())+", " +
                        "Lon: " + Double.toString(location.getLongitude()) );


                //Saveing in memory this user's aproximate location
                sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(context);
                setUserLocation(sharedPreferences,location.getLatitude(), location.getLongitude());
            }
        };

        return locationCallback;
    }

    public boolean isGPSenabled(Context context){

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return isGPSEnabled;
    }

    /*Checking if a service is running. Generally used to check foreground service.*/
    public static boolean isMyServiceRunning(Class<?> serviceClass, Activity mActivity) {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*Start background service*/
    public void starServiceFunc(Context context){
        BackgroundLocationService mLocationService = new BackgroundLocationService();
        Intent mServiceIntent = new Intent(context, mLocationService.getClass());

        //startService(new Intent(this,NewService.class));
        if (!Util.isMyServiceRunning(mLocationService.getClass(), (Activity) context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startService(mServiceIntent);
            }
            Log.d(TAG,"Service started");
        } else {
            Log.d(TAG, "Service already running");
        }
    }

    public double getUnixTimestampFromTrueTime(){
        if(TrueTime.isInitialized()){
            Date trueTime = TrueTime.now();
            return trueTime.getTime();
        }else{
            return 0;
        }
    }

    //
    private void updateNotificationForeground(String info){
        NotificationCompat.Builder notificationBuilder = null;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ////////
        /*CHANGE BELOW VALUES BASED ON THE SET VALUES ON BackgroundLocationService Class*/
        String NOTIFICATION_CHANNEL_ID = "Foreground Notifications";
        int NOTIFICATION_ID = 2;
        ////////

        notificationBuilder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                //.setSound(uri)
                .setSmallIcon(R.mipmap.ic_attac)
                .setContentTitle("ATTAC Alerta de Terremotos")
                .setContentText(info);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSilent(true);
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                i, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder.setContentIntent(contentIntent);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    /*To save the user's location into SharedPreferences*/
    public void setUserLocation(SharedPreferences sharedPreferences, double userLat, double userLon){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("userLat", Double.doubleToRawLongBits(userLat));
        editor.putLong("userLon", Double.doubleToRawLongBits(userLon));
        editor.putInt("lastUserLocationTime",utcNowTimestamp());
        long now = (long) getUnixTimestampFromTrueTime();
        if( now == 0 ){
            editor.putLong("lastUserLocationTimeMs",utcNowTimestampmsecs());
        }
        editor.putLong("lastUserLocationTimeMs",now);
        editor.apply();
    }

    /*Getting the user's location from SharedPreferences
    * It returns a List that contains objects.
    * To obtain defined object, once the list is returned,
    * is mandatory to cast the type of object.*/
    public List<Object> getUserLocation(SharedPreferences mySharedPref){
        double lat =  Double.longBitsToDouble(mySharedPref.getLong("userLat", 0));
        double lon =  Double.longBitsToDouble(mySharedPref.getLong("userLon", 0));
        int lastUserLocationTime = mySharedPref.getInt("lastUserLocationTime",0);
        long lastUserLocationTimeMs = mySharedPref.getLong("lastUserLocationTimeMs",0);
        LatLng latLng = new LatLng(lat, lon);

        List<Object> latLonTime = new ArrayList<>();
        latLonTime.add(latLng);
        latLonTime.add(lastUserLocationTime);
        latLonTime .add(lastUserLocationTimeMs);

        return latLonTime;
    }

    /*Setting the user's POI based on the poiNumber.
    * In order to know the max POI value then go to UserPOI class*/
    public void setUserPOI(SharedPreferences sharedPreferences, LatLng latLng, int poiNumber,String name){
        Log.d(TAG,"Saving -> name: "+name+", lat: "+latLng.latitude + ", lon: "+latLng.longitude );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("userPoiLat"+poiNumber, Double.doubleToRawLongBits(latLng.latitude));
        editor.putLong("userPoiLon"+poiNumber, Double.doubleToRawLongBits(latLng.longitude));
        editor.putString("userPoiName"+poiNumber, name);
        editor.apply();
    }

    /*Returns the user's POI based on the poiNumber.
     * In order to know the max POI value then go to UserPOI class*/
    public List<Object> getUserPOIs(SharedPreferences sharedPreferences, int poiNum){

        double lat =  Double.longBitsToDouble(sharedPreferences.getLong("userPoiLat"+poiNum, -999));
        double lon =  Double.longBitsToDouble(sharedPreferences.getLong("userPoiLon"+poiNum, -999));
        String name = sharedPreferences.getString("userPoiName"+poiNum,"");
        LatLng latLng = new LatLng(lat, lon);

        List<Object> latLonName = new ArrayList<>();
        latLonName.add(latLng);
        latLonName.add(name);

        return latLonName;
    }
    /*Remove all the user POIs*/
    public void clearUserPOIs(SharedPreferences sharedPreferences){



        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int poiNumber = 1 ; poiNumber <= MAX_POI_NUMBERS ; poiNumber++){
            editor.putLong("userPoiLat"+poiNumber, Double.doubleToRawLongBits(-999.00));
            editor.putLong("userPoiLon"+poiNumber, Double.doubleToRawLongBits(-999.00));
            editor.putString("userPoiName"+poiNumber, "");
        }

        editor.apply();
    }

    /*It checks if the intensity threshold value was reached.
    * if so, then it returns true, otherwise, false*/
    public boolean alert(LatLng userLatLon, LatLng epiLatLon, float mag, float depth){
        float distance = distanceTwoPoints(epiLatLon.latitude, epiLatLon.longitude, userLatLon.latitude, userLatLon.longitude);
        int intensity = ipe_allen2012_hyp( distance, mag, depth );

        if ( intensity >= INTENSITY_THRESHOLD){
            //okay, based on intensity
            Log.d(TAG, "Intensity DID reach threshold value: "+intensity);
            return true;
        }else{
            Log.d(TAG, "Intensity didn't reach threshold value: "+intensity);
            return false;
        }
    }

    /*Travel time for S wave, using an aproximate velocity value to some location.
    * this uses the hypocentral distance*/
    public int travelTimeVs(LatLng userLatLon, LatLng epiLatLon, float depth){

        float distance = distanceTwoPoints(epiLatLon.latitude, epiLatLon.longitude, userLatLon.latitude, userLatLon.longitude);
        double hypoDistance = Math.sqrt(Math.pow(distance,2)+Math.pow(depth,2));

        //t  = distance/Vs
        int time = (int) (hypoDistance / VS_VELOCITY);
        return time;

    }

    /*to obtain the max intensity value among all POIs*/
    public List<Object> getMaxIntensityOnPOI(SharedPreferences sharedPreferences, EqInfo eq){
        List<Object> poi = new ArrayList<>();
        LatLng poiLatLonTmp = null;
        String poiNameTmp = null;
        List<Object> poiTmp = null;

        for(int i = 1; i<= MAX_POI_NUMBERS; i++ ){
            poi = getUserPOIs(sharedPreferences,i);
            poi.add(2,-1);
            poiLatLonTmp = (LatLng) poi.get(0);
            poiNameTmp = (String) poi.get(1);

            if( poiNameTmp.equals("")){
                //not valid
                continue;
            }
            int epiDistance = distanceTwoPoints(eq.getLat(), eq.getLon(), poiLatLonTmp.latitude, poiLatLonTmp.longitude);
            int intensity = ipe_allen2012_hyp(epiDistance, eq.getMagnitude(), eq.getDepth());
            Log.d(TAG, "Intensity value for POI: "+poiNameTmp+" is "+intensity);
            poi.add(2,intensity);
            if( intensity >= 1 ){

                if( poiTmp == null){
                    poiTmp = poi;
                }else{
                    if( (int) poi.get(2) > (int)poiTmp.get(2)  ){
                        poiTmp = poi;
                    }else{
                        //
                    }
                }
            }
        }
        //poiTmp.get(0) = LatLon obj
        //poiTmp.get(1) = String obj -> poi name
        //poiTmp.get(2) = int obj -> poi intensity value
        return poiTmp;
    }

    /*it returns a List of objects that contains the POIs' information*/
    public List<Object> getAllPOI(SharedPreferences sharedPreferences){
        List<Object> allPOI= new ArrayList<>();

        for(int i = 1;i<=MAX_POI_NUMBERS; i++){
            allPOI.add(getUserPOIs(sharedPreferences,i));
        }
        return allPOI;
    }

    /*It returns the Intensity Value, Description and color for a defined location.
    * The location can be  user's location if enabled, POIs or near location*/
    public List<Object> getIntensityDescAndColor(SharedPreferences sharedPreference, float epiLat,
                                                 float epiLon, float depth, float magnitude,
                                                 int eqDist, String evtid, boolean locationEnabled )
    {

        Log.d(TAG,"Intensity Desc and Color. is Location enabled?: "+locationEnabled);
        float epiDistance = -1;
        int intensityReported = -1;
        int intensityNearLoc = -1;
        int intensityPoi = -1;
        int intensityUserLoc = -1;
        String poiName = null;
        int intensity = -1;
        boolean estimated = false;
        boolean locEnabled = locationEnabled;

        List<Object> intArr = new ArrayList<>();

        EqDbHelper mEqDbHelper = new EqDbHelper(context);

        int intreported = mEqDbHelper.getMaxIntensityReported(evtid);

        double userLat = Double.longBitsToDouble(sharedPreference.getLong("userLat", 0));
        double userLon = Double.longBitsToDouble(sharedPreference.getLong("userLon", 0));
        long lastUserLocationTimeMs = sharedPreference.getLong("lastUserLocationTimeMs",0);

        if (intreported > 0) {
            estimated = false;
            intensityReported = intreported;
        } else {

            estimated = true;
            long timeDiffMs = 0;
            long now = 0;

            try{
                now = (long) getUnixTimestampFromTrueTime();
            }catch ( Exception e ){
                Log.e(TAG,"Not possible to get time from TrueTime: "+e.toString() );
                now = utcNowTimestampmsecs();
            }

            timeDiffMs = now - lastUserLocationTimeMs;

            if( locEnabled ){
                if (userLat != 0 && userLon != 0 && timeDiffMs < 900 * 1000 ) {
                    Log.d(TAG,"Estimate intensity on User's Location");
                    epiDistance = distanceTwoPoints(epiLat, epiLon, userLat, userLon);
                    intensityUserLoc = ipe_allen2012_hyp(epiDistance, magnitude, depth);
                }
            }
            else {
                //getting intensity on the POIs
                Log.d(TAG,"Estimate intensity on POIs or NearLocation");
                List<Object> allPOIs = getAllPOI(sharedPreference);

                List<Object> tmpObj = null;
                LatLng tmpLatLon = null;
                String tmpName = null;
                int tmpIntensity = -1;


                for (int i = 0; i < MAX_POI_NUMBERS; i++) {
                    tmpObj = (List<Object>) allPOIs.get(i);
                    tmpLatLon = (LatLng) tmpObj.get(0);
                    tmpName = (String) tmpObj.get(1);

                    if (tmpName.equals("")) {
                        continue;
                    }

                    epiDistance = distanceTwoPoints(epiLat, epiLon, tmpLatLon.latitude, tmpLatLon.longitude);
                    tmpIntensity = ipe_allen2012_hyp(epiDistance, magnitude, depth);
                    Log.d(TAG,"Intensity val:"+tmpIntensity+" on: "+tmpName+" "+evtid);
                    if (tmpIntensity > intensityPoi) {
                        intensityPoi = tmpIntensity;
                        poiName = tmpName;
                    }
                }

                try {
                    intensityNearLoc = ipe_allen2012_hyp(eqDist, magnitude, depth);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    intensityNearLoc = 0;
                }
            }


        }

        String intDesc = "--";
        String intColor = "#FFFFFF";
        String intTmp = null;

        if (estimated && locEnabled ) {
            //if user location intensity is available this must be more than -1
            Log.d(TAG, "IntensityUserLoc: " + intensityUserLoc + ", IntensityPOI: " + intensityPoi +
                    ", intensityNearLoc: " + intensityNearLoc);
            intTmp = intensity2RomanDescription(intensityUserLoc);
            intensity = intensityUserLoc;
            intArr.add("myLocation");
        }else if( estimated && !locEnabled ) {
            //it is decided for the max intensity on POI or near location
            if (intensityPoi > -1 ) {
                intTmp = intensity2RomanDescription(intensityPoi);
                intensity = intensityPoi;
                intArr.add(poiName);
            } else {
                intTmp = intensity2RomanDescription(intensityNearLoc);
                intensity = intensityNearLoc;
                intArr.add("nearLocation");
            }
        } else {
            intTmp = intensity2RomanDescription(intensityReported);
            intensity = intensityReported;
            intArr.add("reported");
        }

        int textColor = Color.GRAY;

        if (intensity>0 && intensity < 9){
            //Text color black
            textColor = Color.BLACK;

        }else if(intensity >= 9 ){
            //text color white
            textColor = Color.WHITE;

        }

        intDesc = intTmp.split(";")[0];
        intColor = intTmp.split(";")[1];


        intArr.add(intDesc);
        intArr.add(intColor);
        intArr.add(textColor);

        Log.d(TAG,"Returning from max intensity: "+intArr.get(0)+" "+intDesc+" "+intColor+" "+textColor);
        return intArr;
    }


}
