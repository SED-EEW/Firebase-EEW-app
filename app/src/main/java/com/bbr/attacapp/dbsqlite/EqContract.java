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
* This is the Contract for the Local (in the client's device) Database columns.
*
* */
package com.bbr.attacapp.dbsqlite;
import android.provider.BaseColumns;

public class EqContract {
    public static abstract class EqEntry implements BaseColumns {
        /*Tables Names*/
        public static final String TABLE_NAME = "EqInfo";

        /*Some columns*/
        /*ALl this information will be saved locally in a SQLite Database*/
        public static final String EVTID = "evtid"; /*event id*/
        public static final String MAGNITUDE = "magnitude"; /*magnitude value*/
        public static final String DEPTH = "depth"; /*depth value*/
        public static final String LAT = "lat"; /*lat value in float*/
        public static final String LON = "lon"; /*lon value in float*/
        public static final String LKH = "likelihood"; /*likelihood value - float from 0.0 to 1.00*/
        public static final String ORTIME = "ortime"; /*origin time as an integer in seconds since 1970*/
        public static final String SENTTIME = "senttime"; /*the time on which the alert was sent from EEW server in seconds since 1970*/
        public static final String RECTIME = "rectime"; /*message reception time in seconds since 1970. This is added after the msg is received in the client's app*/
        public static final String AGENCY = "agency"; /*agency identifier*/
        public static final String STATUS = "status"; /* Reviewed, Automatic, Deleted */
        public static final String TYPE = "type"; /* alert= sent through AMQ, feed = collected from feed web service*/
        public static final String LOCATION = "location"; /*location is a string that contains the near epicentral location*/
        public static final String UPDATENO = "updateno"; /*update number received by the client's app*/
        public static final String LASTUPDATE = "lastupdate"; /*the id number of the last update*/
        public static final String ORID = "orid"; /*origin id of the event sent from EEW server*/
        public static final String MAGID = "magid"; /*magnitude id of the event sent from EEW server*/
        public static final String NOTID = "notid"; /*Notification ID to overwrite the notification as new updates arrive*/
        public static final String NUMARRIVALS = "numarrivals"; /*event arrival sent from EEW server*/
        public static final String INTESTIMATED = "intestimated"; /*MM intensity estimated in the user's app through Allen equation */
        public static final String INTREPORTED = "intreported"; /*MM intensity reported by the user through the layout*/
        public static final String USERLAT = "userlat"; /*user lat obtained from GPS */
        public static final String USERLON = "userlon"; /*user lon obtained from GPS */
        public static final String DISTNEARLOC = "distnearloc"; /*distance to the near location in km. it must contain an integer value*/

    }
}
