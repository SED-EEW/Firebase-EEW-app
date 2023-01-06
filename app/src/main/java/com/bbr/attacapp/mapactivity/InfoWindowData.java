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
* This class is for containing information of an event.
* This is mainly instanced by MapActivity and casted as an object
* in the CustomWindowAdapter
*
* */
package com.bbr.attacapp.mapactivity;

public class InfoWindowData {
    private String magnitude;
    private String depth;
    private String nearplace;
    private String datetime;
    private String timespan;
    private String agency;
    private String intensity;
    private String intensityDescription;
    private boolean intReportedByUser;
    private String epiLat;
    private String epiLon;
    private String nearPlaceDist;
    private String evtId;
    private String depthVal;

    public String getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(String mag) {
        this.magnitude = mag;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public String getNearplace() {
        return nearplace;
    }

    public void setNearplace(String nearplace) {
        this.nearplace = nearplace;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getTimespan() {
        return timespan;
    }

    public void setTimespan(String timespan) {
        this.timespan = timespan;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getIntensityDescription() {
        return intensityDescription;
    }

    public void setIntensityDescription(String intensityDescription) {
        this.intensityDescription = intensityDescription;
    }

    public boolean getIntReportedByUser() {
        return intReportedByUser;
    }

    public void setIntReportedByUser(boolean intReportedByUser) {
        this.intReportedByUser = intReportedByUser;
    }

    public String getEpiLat() {
        return epiLat;
    }

    public void setEpiLat(String epiLat) {
        this.epiLat = epiLat;
    }

    public String getEpiLon() {
        return epiLon;
    }

    public void setEpiLon(String epiLon) {
        this.epiLon = epiLon;
    }

    public String getNearPlaceDist() {
        return nearPlaceDist;
    }

    public void setNearPlaceDist(String nearPlaceDist) {
        this.nearPlaceDist = nearPlaceDist;
    }

    public String getEvtId() {
        return evtId;
    }

    public void setEvtId(String evtId) {
        this.evtId = evtId;
    }

    public String getDepthVal() {
        return depthVal;
    }

    public void setDepthVal(String depthVal) {
        this.depthVal = depthVal;
    }
}