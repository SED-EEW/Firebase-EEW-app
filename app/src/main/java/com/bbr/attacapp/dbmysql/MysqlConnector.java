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
* This class is used to connect to MySQL at INETER server.
*
* */
package com.bbr.attacapp.dbmysql;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class MysqlConnector {
    String url = "jdbc:mysql://XXXXXXXXXXXXX:XXXX/seiscomp3";
    String userName = "sysop";
    String password = "";
    Connection connection=null;

    private final String TAG = "mysql connector";

    public Connection connection() {
        try {
            // Loading the controller..
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Log.d(TAG,"instanced successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            // Obtaining the connection to the DB
            connection = DriverManager.getConnection(url, userName, password);
            Log.d(TAG,"Mysql connection is successful");

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return connection;
    }

    public String query(Connection connection){
        String result = null;
        try {

           // connection = DriverManager.getConnection(url, userName, password);
            Log.d(TAG,"Mysql database connection stablished");
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("select * from Magnitude  LIMIT 5");
            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                result += rs.getString(1).toString() + "\n";
                Log.d(TAG,rs.getString(1).toString());
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        String val = result;
        return val;
    }
}
