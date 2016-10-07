package com.draekko.traypreferencesapp;

import android.content.Context;
import android.os.Bundle;

import com.draekko.traypreferences.TrayPreference;
import com.draekko.traypreferences.TrayPreferenceFragment;
import com.draekko.traypreferences.TraySharedPreferences;
import com.draekko.traypreferences.TraySwitchPreference;

public class CategoryOnePreferences extends TrayPreferenceFragment implements
        TraySharedPreferences.OnSharedPreferenceChangeListener, TrayPreference.OnPreferenceChangeListener {

    private static String EVENT_NAME = "CategoryOnePreferences";

    private Context mContext;
    private TraySwitchPreference mDoDebug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_category_one);
        mContext = getActivity();
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

