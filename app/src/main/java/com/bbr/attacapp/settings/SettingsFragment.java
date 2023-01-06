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
* Fragment that implements SharePreferences and its listener.
* it loads default or user's config values.
*
* */


package com.bbr.attacapp.settings;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
//import android.support.annotation.Nullable;
import android.text.method.DigitsKeyListener;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.bbr.attacapp.R;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private String TAG = "SettingsFragment";
    SharedPreferences sharedPreferences ;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        }

        // below line is used to add preference
        // fragment from our xml folder.
        addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());

        final SwitchPreference alertMag =  (SwitchPreference) findPreference("prefNotifSwitch");
        final SwitchPreference alertIntensity = (SwitchPreference) findPreference("prefAlertsSwitch");

        alertMag.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean turned = (Boolean) newValue;
                if (turned) {
                    alertMag.setEnabled(true);
                    alertIntensity.setEnabled(false);
                    preferences.edit().putBoolean("prefAlertsSwitch", false).apply();
                } else {
                    alertIntensity.setEnabled(true);
                }
                preferences.edit().putBoolean("prefNotifSwitch", turned).apply();

                return true;
            }
        });

        alertIntensity.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean turned = (Boolean) newValue;
                if (turned) {
                    alertMag.setEnabled(false);
                    alertIntensity.setEnabled(true);
                    preferences.edit().putBoolean("prefNotifSwitch", false).apply();
                } else {
                    alertMag.setEnabled(true);
                }
                //
                preferences.edit().putBoolean("prefAlertsSwitch", turned).apply();
                return true;
            }
        });

    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onResume(){
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefMinMagNoti");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefMaxDepthNoti");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefNotiAgency");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefFiltMinMag");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefFiltMaxDepth");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefFiltMaxDaysAgo");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefFiltAgency");
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), "prefAlertMinIntensity");



    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
        Log.d(TAG,"changed preferences");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("prefChanged",true);
        editor.apply();
    }

    private void initSummary(Preference p){
        if (p instanceof PreferenceCategory){
            PreferenceCategory pCat = (PreferenceCategory)p;
            for(int i=0;i<pCat.getPreferenceCount();i++){
                initSummary(pCat.getPreference(i));
            }
        }else{
            updatePrefSummary(p);
        }

    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
            String key = p.getKey();
            String tmp = ((ListPreference) p).getValue();
            Log.d(TAG,"key: "+key+", value: "+tmp);
            if (key.equals("prefNotiAgency") || key.equals("prefFiltAgency")){
                if (tmp.equals("all")){
                    p.setIcon(R.mipmap.ic_attac);
                }
                if (tmp.equals("insivumeh")){
                    p.setIcon(R.mipmap.ic_insivumeh);
                }
                if (tmp.equals("UNA")){
                    p.setIcon(R.mipmap.ic_ovsicori);
                }
                if (tmp.equals("INETER")){
                    p.setIcon(R.mipmap.ic_ineter);
                }
                if (tmp.equals("MARN")){
                    p.setIcon(R.mipmap.ic_marn);
                }
            }
            //
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getKey().equalsIgnoreCase("editKey")) {
                p.setSummary("No password display!");
            } else {
                p.setSummary(editTextPref.getText());
            }
        }
    }


}