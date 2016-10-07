/*
 * Copyright (C) 2016 Benoit Touchette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.draekko.traypreferencesapp;

import android.content.Context;
import android.os.Bundle;

import com.draekko.traypreferences.TrayPreference;
import com.draekko.traypreferences.TrayPreferenceFragment;
import com.draekko.traypreferences.TraySharedPreferences;
import com.draekko.traypreferences.TraySwitchPreference;
import com.draekko.traypreferencesapp.misc.Constants;
import com.draekko.traypreferencesapp.misc.Debug;


// =========================================================================
// ==[ CLASS ]==============================================================
// =========================================================================

public class AdvancedPreferences extends TrayPreferenceFragment implements
        TraySharedPreferences.OnSharedPreferenceChangeListener, TrayPreference.OnPreferenceChangeListener {

    private static String EVENT_NAME = "AdvancedPreferences";

    private Context mContext;
    private TraySwitchPreference mDoDebug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_advanced);
        mContext = getActivity();

        mDoDebug = (TraySwitchPreference) findPreference(Constants.USE_DEBUGGER);

        Boolean defValue = Debug.doDebug(mContext);
        Debug.setDoDebug(mContext, defValue);
        mDoDebug.setChecked(defValue);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(TraySharedPreferences prefs, String key) {
    }

    @Override
    public boolean onPreferenceChange(TrayPreference preference, Object newValue) {
        return false;
    }
}
