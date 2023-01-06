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
* Welcome Class is to create a simple welcome activity with a message.
* it renders the welcome.xml file and there is a simple button to change the activity to the
* check the location permissions
*
* */
package com.bbr.attacapp.startactivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bbr.attacapp.R;
import com.bbr.attacapp.permissions.LocPermissionActivity;

public class Welcome extends AppCompatActivity {

    private Button  nextButton ;
    private SharedPreferences myShared;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        nextButton = (Button) findViewById(R.id.welcomeButton);
        myShared = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = myShared.getBoolean("welcome", true );
        if( !firstTime ){
            changeActivity();
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeActivity();
            }
        });
    }
    private void changeActivity(){
        Intent i = new Intent(Welcome.this, LocPermissionActivity.class);
        Log.d("Welcome","starting a new activity: LocPermissionActivity");
        startActivity(i);
        SharedPreferences.Editor editor = myShared.edit();
        editor.putBoolean("welcome",false);
        editor.apply();
        finish();
    }

}
