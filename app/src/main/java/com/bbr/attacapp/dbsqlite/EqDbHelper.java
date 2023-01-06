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
* This class is related to DB queries, the creating of this and its table,
* update uit in case that something changes on this (Altering the table, removing it, and more).
* Relevant queries are in this class as method.
*
* */
package com.bbr.attacapp.dbsqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bbr.attacapp.utilitary.EqInfo;
import com.bbr.attacapp.dbsqlite.EqContract.EqEntry;
import com.bbr.attacapp.utilitary.Util;

public class EqDbHelper extends SQLiteOpenHelper {

    private String TAG = "EQDbHelper";
    private Util util;
    Context lcontext;

    /*Preliminary Version of this DB*/
    /*If the DB is updated, then the value must increase!*/
    public static final int DATABASE_VERSION = 8;

    /*DB NAME!*/
    public static final String DATABASE_NAME = "eqs.db";

    public EqDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        lcontext = context;
        util = new Util(lcontext);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("database","querying creating table....");
        db.execSQL("CREATE TABLE " + EqEntry.TABLE_NAME + " ("
                + EqEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EqEntry.EVTID + " TEXT NOT NULL,"
                + EqEntry.MAGNITUDE + " REAL NOT NULL,"
                + EqEntry.DEPTH + " REAL NOT NULL,"
                + EqEntry.LAT + " REAL NOT NULL,"
                + EqEntry.LON + " REAL NOT NULL,"
                + EqEntry.LKH + " REAL NOT NULL,"
                + EqEntry.ORTIME + " INTEGER NOT NULL,"
                + EqEntry.SENTTIME + " INTEGER NOT NULL,"
                + EqEntry.RECTIME + " INTEGER NOT NULL,"
                + EqEntry.AGENCY + " TEXT NOT NULL,"
                + EqEntry.STATUS + " TEXT NOT NULL,"
                + EqEntry.TYPE + " TEXT NOT NULL,"
                + EqEntry.LOCATION + " TEXT NOT NULL,"
                + EqEntry.UPDATENO + " INTEGER NOT NULL,"
                + EqEntry.LASTUPDATE + " INTEGER NOT NULL,"
                + EqEntry.ORID + " TEXT,"
                + EqEntry.MAGID + " TEXT,"
                + EqEntry.NUMARRIVALS + " INTEGER,"
                + EqEntry.NOTID + " INTEGER NOT NULL,"
                + EqEntry.INTESTIMATED + " INTEGER DEFAULT -1,"
                + EqEntry.INTREPORTED + " INTEGER DEFAULT -1,"
                + EqEntry.USERLAT + " REAL DEFAULT -999.00,"
                + EqEntry.USERLON + " REAL DEFAULT -999.00,"
                + EqEntry.DISTNEARLOC + " INTEGER DEFAULT -1)");
                //+ "UNIQUE (" + EqEntry.EVTID + "))");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        /*
        * if it is needed to update the table, then you can do it right here.
        * before this changes are applied the DB version must changed in DATABASE_VERSION global variable.
        */
        if (newVersion > oldVersion) {
            Log.d(TAG,"***********ALTERING DATABASE!**************");
            String Query = "ALTER TABLE EqInfo ADD COLUMN intestimated INTEGER DEFAULT -1";
            Log.d(TAG,"Querying: "+Query);
            try{
                sqLiteDatabase.execSQL(Query);
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
            Query = "ALTER TABLE EqInfo ADD COLUMN intreported INTEGER DEFAULT -1";
            Log.d(TAG,"Querying: "+Query);
            try{
                sqLiteDatabase.execSQL(Query);
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
            Query = "ALTER TABLE EqInfo ADD COLUMN userlat REAL DEFAULT -999.00";
            Log.d(TAG,"Querying: "+Query);
            try{
                sqLiteDatabase.execSQL(Query);
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
            Query = "ALTER TABLE EqInfo ADD COLUMN userlon REAL DEFAULT -999.00";
            Log.d(TAG,"Querying: "+Query);
            try{
                sqLiteDatabase.execSQL(Query);
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }

            Query = "ALTER TABLE EqInfo ADD COLUMN distnearloc INTEGER DEFAULT -1";
            Log.d(TAG,"Querying: "+Query);
            try{
                sqLiteDatabase.execSQL(Query);
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }


        }else{
            Log.d(TAG,"No need to updatedb");
        }
    }

    /*//////////////////////////////////////////////////////////////
                                DATABASE QUERIES
     //////////////////////////////////////////////////////////////*/

    public long saveEq(EqInfo eqinfo) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                EqEntry.TABLE_NAME,
                null,
                eqinfo.toContentValues());

    }

    public Cursor getAllEqsList() {
       return getReadableDatabase()
               .query(
                       EqEntry.TABLE_NAME,
                       null,
                       null,
                       null,
                       null,
                       null,
                       "ORTIME DESC"); /* sorted by origin time in descending order*/

    }
    public Cursor getAllEqs() {
        String[] param = null;
         return getReadableDatabase().rawQuery("select whole.* from "+EqEntry.TABLE_NAME+" whole WHERE whole.updateno = (SELECT MAX(updateno) from "+EqEntry.TABLE_NAME+" WHERE evtid=whole.evtid) ORDER BY whole.rectime DESC, whole.ortime DESC",param);

    }
    public Cursor getLastEq() {

        String[] param = null;
        return getReadableDatabase().rawQuery("select whole.* from "+EqEntry.TABLE_NAME+" whole WHERE whole.updateno = (SELECT MAX(updateno) from "+EqEntry.TABLE_NAME+" WHERE evtid=whole.evtid) ORDER BY whole.rectime DESC, whole.ortime DESC LIMIT 1",param);
    }

    public Cursor getEqById(String eqId) {

        String[] param = new String[1];
        param[0] = eqId;
        return getReadableDatabase().rawQuery("select * from "+EqEntry.TABLE_NAME+" WHERE evtid = ? AND updateno = (SELECT MAX(updateno) from "+EqEntry.TABLE_NAME+" where evtid = ?)",new String[]{eqId,eqId});

    }
    public Cursor getEqAllById(String eqId){
        Cursor c = getReadableDatabase().query(
                        EqEntry.TABLE_NAME,
                        null,
                        EqEntry.EVTID + " LIKE ?",
                        new String[]{eqId},
                        null,
                        null,
                        null);
        return c;
    }

    public Cursor getDataFromUnixtimeEqs(int unixtime){
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM EqInfo WHERE ORTIME >=? ORDER BY ORTIME DESC",
                new String[] { String.valueOf(unixtime)});
        return c;
    }

    public Cursor getFilteredEqs(){
        String filtMinMag = String.valueOf( util.readPrefFloatMag(lcontext, "prefFiltMinMag","default"));
        String filtMaxDepth = String.valueOf( util.readPrefIntDepth(lcontext, "prefFiltMaxDepth","default"));
        int daysAgo = util.readPrefIntDaysAgo(lcontext,"prefFiltMaxDaysAgo","default");

        int daysAgoSeconds = daysAgo*24*60*60;
        int now = util.utcNowTimestamp();
        int diff = now - daysAgoSeconds;
        String strDiff = String.valueOf(diff);

        String agency = util.readPrefStrAgency(lcontext,"prefFiltAgency","default");
        Log.d(TAG,filtMinMag+", "+filtMaxDepth+", "+daysAgo+", "+strDiff+", "+agency );
        Cursor c = null;
        if (agency.equals("all")){
            c = getReadableDatabase().rawQuery(
                    "SELECT whole.* FROM EqInfo whole WHERE whole.MAGNITUDE>=? AND whole.DEPTH<=? AND whole.ORTIME >=? AND whole.updateno = (SELECT MAX(updateno) from EqInfo WHERE evtid=whole.evtid) ORDER BY whole.ORTIME DESC",
                    new String[] { filtMinMag, filtMaxDepth, strDiff}
            );
        }else{
            c = getReadableDatabase().rawQuery(
                    "SELECT whole.* FROM EqInfo whole WHERE whole.MAGNITUDE>=? AND whole.DEPTH <=? AND whole.ORTIME >=? AND whole.agency =? AND whole.updateno = (SELECT MAX(updateno) from EqInfo WHERE evtid=whole.evtid) ORDER BY ORTIME DESC",
                    new String[] { filtMinMag, filtMaxDepth, strDiff, agency}
            );
        }
        return  c;
    }

    @SuppressLint("Range")
    public int getMaxIntensityReported(String evtId){
        int intensity = -1;
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM EqInfo WHERE evtid=? AND intreported = (SELECT MAX(intreported) FROM EqInfo WHERE evtid=?) LIMIT 1",
                new String[] { evtId, evtId}
        );
        if(c.getCount() >= 1) {
            c.moveToNext();
        }else{
            return -1;
        }
        try{
            intensity = c.getInt(c.getColumnIndex(EqContract.EqEntry.INTREPORTED));
            Log.d(TAG,"Intensity reported from DB: "+intensity);
        } catch (Exception e) {
            Log.e(TAG,"Error while obtaning the Intensity reported for event "+evtId);
            e.printStackTrace();

        }

        return intensity;
    }

    public Cursor getLastFilteredEq(){
        String filtMinMag = String.valueOf( util.readPrefFloatMag(lcontext, "prefFiltMinMag","default"));
        String filtMaxDepth = String.valueOf( util.readPrefIntDepth(lcontext, "prefFiltMaxDepth","default"));
        int daysAgo = util.readPrefIntDaysAgo(lcontext,"prefFiltMaxDaysAgo","default");

        int daysAgoSeconds = daysAgo*24*60*60;
        int now = util.utcNowTimestamp();
        int diff = now - daysAgoSeconds;
        String strDiff = String.valueOf(diff);

        String agency = util.readPrefStrAgency(lcontext,"prefFiltAgency","default");

        Log.d(TAG,"Filtered event");
        Log.d(TAG,filtMinMag+", "+filtMaxDepth+", "+daysAgo+", "+strDiff+", "+agency );
        Cursor c = null;
        if (agency.equals("all")){
            c = getReadableDatabase().rawQuery(
                    "SELECT whole.* FROM EqInfo whole WHERE whole.MAGNITUDE>=? AND whole.DEPTH<=? AND whole.ORTIME >=? AND whole.updateno = (SELECT MAX(updateno) from EqInfo WHERE evtid=whole.evtid) ORDER BY whole.ORTIME DESC LIMIT 1",
                    new String[] { filtMinMag, filtMaxDepth, strDiff}
            );
        }else{
            c = getReadableDatabase().rawQuery(
                    "SELECT whole.* FROM EqInfo whole WHERE whole.MAGNITUDE>=? AND whole.DEPTH <=? AND whole.ORTIME >=? AND whole.agency =? AND whole.updateno = (SELECT MAX(updateno) from EqInfo WHERE evtid=whole.evtid) ORDER BY ORTIME DESC LIMIT 1",
                    new String[] { filtMinMag, filtMaxDepth, strDiff, agency}
            );
        }
        return  c;
    }

    public int deleteEq(String evtId) {
        return getWritableDatabase().delete(
                EqEntry.TABLE_NAME,
                EqEntry.EVTID + " LIKE ?",
                new String[]{evtId});
    }

    public int deleteTestEqs(String location) {
        return getWritableDatabase().delete(
                EqEntry.TABLE_NAME,
                EqEntry.LOCATION + " = ?",
                new String[]{location});
    }

    public int deleteByUnixTimestamp(int mindatetime){
        return getWritableDatabase().delete(
                EqEntry.TABLE_NAME,
                EqEntry.ORTIME + " < ?",
                new String[]{String.valueOf(mindatetime)});
    }

    public int updateEq(EqInfo eqinfo, String evtId) {
        return getWritableDatabase().update(
                EqEntry.TABLE_NAME,
                eqinfo.toContentValues(),
                EqEntry.EVTID + " LIKE ?",
                new String[]{evtId}
        );
    }

    public int updateReportedIntensity(EqInfo eqinfo, int intensityVal, String evtid, int updateno){
        Log.i(TAG,"Update Number:"+eqinfo.getUpdate());
        Log.i(TAG,"user lon:"+eqinfo.getUserlon());
        Log.i(TAG,"user lat:"+eqinfo.getUserlat());
        Log.i(TAG,"evitd:"+evtid);
        return getWritableDatabase().update(
                EqEntry.TABLE_NAME,
                eqinfo.toContentValues(),
                EqEntry.EVTID + " = ?"+" AND "+EqEntry.UPDATENO+" = ?",
                new String[]{ evtid, String.valueOf(updateno) }
        );
    }

    /*private void mockData(SQLiteDatabase sqLiteDatabase) {
        mockEq(sqLiteDatabase, new EqInfo("una2022sd", 7.5f, 35, 16.25f, -68.54f,
                0.4f, 1647456893, 1647456898, 1647456902,
        "ovsicori", "automatic", "alert", "55 km al sur de Managua, Nicaragua"));
        mockEq(sqLiteDatabase, new EqInfo("una2022ssdd", 6.5f, 35, 16.25f, -68.54f,
                0.4f, 1647457993, 1647457898, 1647457902,
                "ovsicori", "automatic", "alert", "34 km al sur de Managua, Nicaragua"));
        mockEq(sqLiteDatabase, new EqInfo("una2022ss2d", 5.5f, 35, 16.25f, -68.54f,
                0.4f, 1647457995, 1647457898, 1647457902,
                "ovsicori", "automatic", "alert", "34 km al sur de Managua, Nicaragua"));

    }*/

    public long mockEq(SQLiteDatabase db, EqInfo eqinfo) {
        return db.insert(
                EqEntry.TABLE_NAME,
                null,
                eqinfo.toContentValues());
    }

    public void getId(){
        Cursor c = getReadableDatabase().rawQuery("SELECT _id from "+EqEntry.TABLE_NAME,new String[]{});
        while (c.moveToNext()) {
           // Log.d(TAG,c.getString(0));
        }
    }
}
