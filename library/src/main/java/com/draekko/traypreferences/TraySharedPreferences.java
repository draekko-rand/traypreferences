/*
 * Copyright (C) 2006 The Android Open Source Project
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

import java.util.Map;
import java.util.Set;

/**
 * Interface for accessing and modifying preference data returned by {link
 * Context#getSharedPreferences}.  For any particular set of preferences,
 * there is a single instance of this class that all clients share.
 *
 * <p><em>Note: This class does not support use across multiple processes.</em>
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For more information about using SharedPreferences, read the
 * <a href="{@docRoot}guide/topics/data/data-storage.html#pref">Data Storage</a>
 * developer guide.</p></div>
 *
 * see Context#getSharedPreferences
 */
public interface TraySharedPreferences {

    /**
     * Interface definition for a callback to be invoked when a shared
     * preference is changed.
     */
    public interface OnSharedPreferenceChangeListener {
        /**
         * Called when a shared preference is changed, added, or removed. This
         * may be called even if a preference is set to its existing value.
         *
         * <p>This callback will be run on your main thread.
         *
         * @param sharedPreferences The {@link TraySharedPreferences} that received
         *            the change.
         * @param key The key of the preference that was changed, added, or
         *            removed.
         */
        void onSharedPreferenceChanged(TraySharedPreferences sharedPreferences, String key);
    }

    /**
     * Get TraySharedPreferences reference.
     * @param context
     */
    static AppPreferences getPrefs(Context context) {
        return null;
    };

    /**
     * Set the static context for shared preferences.
     * @param context
     */
    void setContext(Context context);

    /**
     * Interface used for modifying values in a {@link TraySharedPreferences}
     * object.  All changes you make in an editor are batched, and not copied
     * back to the original {@link TraySharedPreferences}.
     */

    /**
     * Set a String value in the preferences editor.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void putString(String key, @Nullable String value);

    /**
     * Set a set of String values in the preferences editor.
     *
     * @param key The name of the preference to modify.
     * @param values The set of new values for the preference.  Passing {@code null}
     *    for this argument is equivalent to calling {@link #remove(String)} with
     *    this key.
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void putStringSet(String key, @Nullable Set<String> values);

    /**
     * Set an int value in the preferences editor.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void putInt(String key, int value);

    /**
     * Set a long value in the preferences editor.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void putLong(String key, long value);

    /**
     * Set a float value in the preferences editor.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void putFloat(String key, float value);

    /**
     * Set a boolean value in the preferences editor.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     *
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void putBoolean(String key, boolean value);

    /**
     * Mark in the editor that a preference value should be removed.
     *
     * <p>Note that when committing back to the preferences, all removals
     * are done first, regardless of whether you called remove before
     * or after put methods on this editor.
     *
     * @param key The name of the preference to remove.
     *
     * @return Returns a reference to the same Editor object, so you can
     * chain put calls together.
     */
    void remove(String key);

    /**
     * Retrieve all values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the collection returned
     * by this method, or alter any of its contents.  The consistency of your
     * stored data is not guaranteed if you do.
     *
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     *
     * @throws NullPointerException
     */
    Map<String, ?> getAll();

    /**
     * Retrieve a String value from the preferences.
     * 
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * 
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     * 
     * @throws ClassCastException
     */
    @Nullable
    String getString(String key, @Nullable String defValue);
    
    /**
     * Retrieve a set of String values from the preferences.
     * 
     * <p>Note that you <em>must not</em> modify the set instance returned
     * by this call.  The consistency of the stored data is not guaranteed
     * if you do, nor is your ability to modify the instance at all.
     *
     * @param key The name of the preference to retrieve.
     * @param defValues Values to return if this preference does not exist.
     * 
     * @return Returns the preference values if they exist, or defValues.
     * Throws ClassCastException if there is a preference with this name
     * that is not a Set.
     * 
     * @throws ClassCastException
     */
    @Nullable
    Set<String> getStringSet(String key, @Nullable Set<String> defValues);
    
    /**
     * Retrieve an int value from the preferences.
     * 
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * 
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * an int.
     * 
     * @throws ClassCastException
     */
    int getInt(String key, int defValue);
    
    /**
     * Retrieve a long value from the preferences.
     * 
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * 
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a long.
     * 
     * @throws ClassCastException
     */
    long getLong(String key, long defValue);
    
    /**
     * Retrieve a float value from the preferences.
     * 
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * 
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a float.
     * 
     * @throws ClassCastException
     */
    float getFloat(String key, float defValue);
    
    /**
     * Retrieve a boolean value from the preferences.
     * 
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * 
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a boolean.
     * 
     * @throws ClassCastException
     */
    boolean getBoolean(String key, boolean defValue);

    /**
     * Checks whether the preferences contains a preference.
     * 
     * @param key The name of the preference to check.
     * @return Returns true if the preference exists in the preferences,
     *         otherwise false.
     */
    boolean contains(String key);

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * <p class="caution"><strong>Caution:</strong> The preference manager does
     * not currently store a strong reference to the listener. You must store a
     * strong reference to the listener, or it will be susceptible to garbage
     * collection. We recommend you keep a reference to the listener in the
     * instance data of an object that will exist as long as you need the
     * listener.</p>
     *
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);
    
    /**
     * Unregisters a previous callback.
     * 
     * @param listener The callback that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);

    /**
     * Get current shared preferences.
     * @param context
     * @return
     */
    TraySharedPreferences getTraySharedPreferences(Context context);
}
