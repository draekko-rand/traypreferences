/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Portions Copyright (C) 2016 Benoit Touchette
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

package com.draekko.traypreferencesapp.misc;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

public class Preferences {

    private static final String EVENT_NAME = "Preferences";

    private static AppPreferences preference;

    private Preferences(Context context) {
        preference = new AppPreferences(context.getApplicationContext());
    }

    public static boolean doDebug(Context context) {
        return getPrefs(context.getApplicationContext()).getBoolean(Constants.USE_DEBUGGER, false);
    }

    public static void setDoDebug(Context context, boolean debug) {
        getPrefs(context.getApplicationContext()).put(Constants.USE_DEBUGGER, debug);
    }

    public static AppPreferences getPrefs(Context context) {
        if (preference == null) {
            preference = new AppPreferences(context.getApplicationContext());
        }
        return preference;
    }
}