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
 * Basic class to contain EQ information or create it.
 * it has two constructors with the next input variables:
 * 1. Input values with the event information coming from EEW push notification.
 * 2. Cursor Object that is generally obtained from a query response from DB.
 *
 *
 * */
package com.bbr.attacapp.utilitary;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import com.bbr.attacapp.dbsqlite.EqContract.EqEntry;

public class EqInfo {
    private String evtId;
    private float magnitude;
    private float depth;
    private float lat;
    private float lon;
    private float likelihood;
    private int orTime;
    private long sentTime;
    private long recTime;
    private String agency;
    private String status; /* Reviewed, Automatic, Deleted */
    private String type; /* alert= sent through AMQ, feed = collected from feed web service*/
    private String location;
    private int updateno;
    private int lastupdate;
    private String magid;
    private String orid;
    private int notid;
    private int numarrivals;
    private int intestimated;
    private int intreported;
    private double userlat;
    private double userlon;
    private int nearPlaceDist;

    //Constructor with the most basic information - Based on Alert Info
    //It does not contain update number but this can be set and get
    //and also
    public EqInfo(String evtId, float magnitude, float depth,
                  float lat, float lon, float likelihood,
                  int orTime, long sentTime, long recTime,
                  String agency, String status, String type, String location,String oid, String mid, int numarrivals, int nearPlaceDist){
        this.evtId = evtId;
        this.magnitude = magnitude;
        this.depth = depth;
        this.lat = lat;
        this.lon = lon;
        this.likelihood = likelihood;
        this.orTime = orTime;
        this.sentTime = sentTime;
        this.recTime = recTime;
        this.agency = agency;
        this.status = status;
        this.type = type;
        this.location = location;
        this.updateno = 0;
        this.lastupdate = 0;
        this.orid = oid;
        this.magid = mid;
        this.numarrivals = numarrivals;
        this.intestimated = -1;
        this.intreported = -1;
        this.userlat = -999.00;
        this.userlon = -999.00;
        this.nearPlaceDist = nearPlaceDist;


    }
    /*
    * This is to create an eqInfo object from cursor.
    * Generally this comes from DB
    */
    @SuppressLint("Range")
    public EqInfo(Cursor cursor) {
        evtId = cursor.getString(cursor.getColumnIndex(EqEntry.EVTID));
        magnitude = cursor.getFloat(cursor.getColumnIndex(EqEntry.MAGNITUDE));
        lat = cursor.getFloat(cursor.getColumnIndex(EqEntry.LAT));
        lon = cursor.getFloat(cursor.getColumnIndex(EqEntry.LON));
        depth = cursor.getFloat(cursor.getColumnIndex(EqEntry.DEPTH));
        likelihood = cursor.getFloat(cursor.getColumnIndex(EqEntry.LKH));
        orTime = cursor.getInt(cursor.getColumnIndex(EqEntry.ORTIME));
        sentTime = cursor.getLong(cursor.getColumnIndex(EqEntry.SENTTIME));
        if (String.valueOf(sentTime).length() < 13 ){
            sentTime = sentTime*1000;
        }
        recTime = cursor.getLong(cursor.getColumnIndex(EqEntry.RECTIME));
        if (String.valueOf(recTime).length() < 13 ){
            recTime = recTime*1000;
        }
        agency = cursor.getString(cursor.getColumnIndex(EqEntry.AGENCY));
        status = cursor.getString(cursor.getColumnIndex(EqEntry.STATUS));
        type = cursor.getString(cursor.getColumnIndex(EqEntry.TYPE));
        location = cursor.getString(cursor.getColumnIndex(EqEntry.LOCATION));
        updateno = cursor.getInt(cursor.getColumnIndex(EqEntry.UPDATENO));
        lastupdate = cursor.getInt(cursor.getColumnIndex(EqEntry.LASTUPDATE));
        orid = cursor.getString(cursor.getColumnIndex(EqEntry.ORID));
        magid = cursor.getString(cursor.getColumnIndex(EqEntry.MAGID));
        notid = cursor.getInt(cursor.getColumnIndex(EqEntry.NOTID));
        numarrivals = cursor.getInt(cursor.getColumnIndex(EqEntry.NUMARRIVALS));
        intestimated = cursor.getInt(cursor.getColumnIndex(EqEntry.INTESTIMATED));
        intreported = cursor.getInt(cursor.getColumnIndex(EqEntry.INTREPORTED));
        userlat = cursor.getDouble(cursor.getColumnIndex(EqEntry.USERLAT));
        userlon = cursor.getDouble(cursor.getColumnIndex(EqEntry.USERLON));
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put( EqEntry.EVTID, evtId );
        values.put( EqEntry.MAGNITUDE, magnitude );
        values.put( EqEntry.DEPTH, depth );
        values.put( EqEntry.LAT, lat );
        values.put( EqEntry.LON, lon );
        values.put( EqEntry.LKH, likelihood );
        values.put( EqEntry.ORTIME, orTime );
        values.put( EqEntry.SENTTIME, sentTime );
        values.put( EqEntry.RECTIME, recTime );
        values.put( EqEntry.AGENCY, agency );
        values.put( EqEntry.STATUS, status );
        values.put( EqEntry.TYPE, type );
        values.put( EqEntry.LOCATION, location );
        values.put( EqEntry.UPDATENO, updateno);
        values.put( EqEntry.LASTUPDATE, lastupdate);
        values.put( EqEntry.ORID, orid);
        values.put( EqEntry.MAGID, magid);
        values.put( EqEntry.NOTID, notid);
        values.put( EqEntry.NUMARRIVALS, numarrivals);
        values.put( EqEntry.INTESTIMATED, intestimated);
        values.put( EqEntry.INTREPORTED, intreported);
        values.put( EqEntry.USERLAT, userlat);
        values.put( EqEntry.USERLON, userlon);

        return values;
    }

    public String getEvtId() {
        return evtId;
    }

    public void setEvtId(String evtId) {
        this.evtId = evtId;
    }

    public float getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLikelihood() {
        return likelihood;
    }

    public void setLikelihood(float likelihood) {
        this.likelihood = likelihood;
    }

    public int getOrTime() {
        return orTime;
    }

    public void setOrTime(int orTime) {
        this.orTime = orTime;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public long getRecTime() {
        return recTime;
    }

    public void setRecTime(long recTime) {
        this.recTime = recTime;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String type) {
        this.location = location;
    }

    public int getUpdate() {
        return updateno;
    }

    public void setUpdate(int update) {
        this.updateno = update;
    }

    public int getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(int lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getMagid(){ return magid; }

    public String getOrid(){ return orid; }

    public void setMagid(String magid){
        this.magid = magid;
    }

    public void setOrid(String orid){
        this.orid = orid;
    }

    public int getNotid(){
        return notid;
    }

    public void setNotid(int notid){
        this.notid = notid;
    }

    public int getNumarrivals(){
        return numarrivals;
    }

    public void setNumarrivals(int numarrivals){
        this.numarrivals = numarrivals;
    }
    public int getIntestimated(){
        return intestimated;
    }

    public void setIntestimated(int intestimated){
        this.intestimated = intestimated;
    }

    public int getIntreported(){
        return intreported;
    }

    public void setIntreported(int intreported){
        this.intreported = intreported;
    }

    public double getUserlat(){
        return userlat;
    }

    public void setUserlat(double userlat){
        this.userlat = userlat;
    }

    public double getUserlon(){
        return userlon;
    }

    public void setUserlon(double userlon){
        this.userlon = userlon;
    }

    public int getNearPlaceDist(){
        return nearPlaceDist;
    }

    public void setNearPlaceDist(int distNearPlace){
        this.nearPlaceDist = distNearPlace;
    }

}
