/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.draekko.traypreferences;

import android.content.Context;

import com.draekko.traypreferences.annotations.Nullable;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.TrayItem;

import org.json.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


final class TraySharedPreferencesImpl implements TraySharedPreferences {
    private static final String TAG = "TraySharedPreferencesImpl";
    private static final boolean DEBUG = false;

    private static AppPreferences appPreferences;
    private static Context mContext;
    private static TraySharedPreferences traySharedPreferences;

    private Map<String, Object> mMap;
    private static final Object mContent = new Object();
    //private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners =
    //        new WeakHashMap<OnSharedPreferenceChangeListener, Object>();
    private static List<OnSharedPreferenceChangeListener> mListeners =
            new ArrayList<OnSharedPreferenceChangeListener>();

    public TraySharedPreferencesImpl(Context context) {
        mContext = context;
        appPreferences = new AppPreferences(mContext);
    }

    public void setContext(Context context) {
        mContext = context;
        appPreferences = new AppPreferences(mContext);
    }

    public static TraySharedPreferences getPrefs() {
        return getPrefs(mContext);
    }

    public static TraySharedPreferences getPrefs(Context context) {
        if (context == null) {
            return null;
        }
        if (mContext == null) {
            mContext = context;
        }
        if (appPreferences == null) {
            appPreferences = new AppPreferences(context);
        }
        return traySharedPreferences;//appPreferences;
    }

    public void notifyListeners(final String key) {
        if (mListeners != null) {
            for (OnSharedPreferenceChangeListener listener : mListeners) {
                listener.onSharedPreferenceChanged(this, key);
            }
        }
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized(this) {
            mListeners.add(listener);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized(this) {
            mListeners.remove(listener);
        }
    }

    public Map<String, ?> getAll() {
        Map<String, Object> newMap = null;
        Collection<TrayItem> itemCollection = appPreferences.getAll();
        if (itemCollection != null && itemCollection.size() > 0) {
            newMap = new HashMap<>();
            for (TrayItem trayItem : itemCollection) {
                Object value = (Object)trayItem.value();
                newMap.put(trayItem.key(), value);
            }
        }
        return newMap;
    }

    @Nullable
    public String getString(String key, @Nullable String defValue) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        return appPreferences.getString(key, defValue);
    }

    @Nullable
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        Set<String> newVal = null;
        JSONObject jsonObject;

        String val = appPreferences.getString(key, null);
        if (val == null || val.isEmpty()) {
            return defValues;
        }

        newVal = new HashSet<>();
        try {
            jsonObject = new JSONObject(val);
            JSONArray stringset = jsonObject.getJSONArray("stringset");
            for (int loop = 0; loop < stringset.length(); loop++) {
                String data = stringset.getString(loop);
                newVal.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return defValues;
        }
        return newVal;
    }

    public int getInt(String key, int defValue) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        return appPreferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        return appPreferences.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        return appPreferences.getFloat(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        return appPreferences.getBoolean(key, defValue);
    }

    public boolean contains(String key) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        return appPreferences.contains(key);
    }

    public void putString(String key, @Nullable String value) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        appPreferences.put(key, value);
    }

    public void putStringSet(String key, @Nullable Set<String> values) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        if (values == null || values.size() < 1) {
            appPreferences.put(key, null);
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (String s : values) {
            jsonArray.put(s);
        }

        JSONObject json = new JSONObject();
        try {
            json.put("stringset", jsonArray);
            String saved = json.toString();
            appPreferences.put(key, saved);
        } catch (JSONException e) {
            e.printStackTrace();
            appPreferences.put(key, null);
        }
    }

    public void putInt(String key, int value) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        appPreferences.put(key, value);
    }

    public void putLong(String key, long value) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        appPreferences.put(key, value);
    }

    public void putFloat(String key, float value) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        appPreferences.put(key, value);
    }

    public void putBoolean(String key, boolean value) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        appPreferences.put(key, value);
    }

    public void  remove(String key) {

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("invalid key parameter!");
        }

        notifyListeners(key);

        appPreferences.remove(key);
    }

    public TraySharedPreferences getTraySharedPreferences(Context context) {
        if (traySharedPreferences == null) {
            traySharedPreferences = new TraySharedPreferencesImpl(context);
        }
        return traySharedPreferences;
    }

    private static class StringSingle {
        private final String value;

        public StringSingle(String init) {
            this.value = init;
        }

        public String getString() {
            return this.value;
        }
    }
}
