/*
 * Copyright (C) 2007 The Android Open Source Project
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

import com.draekko.traypreferences.annotations.SystemApi;
import com.draekko.traypreferences.annotations.XmlRes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Used to help create {@link TrayPreference} hierarchies
 * from activities or XML.
 * <p>
 * In most cases, clients should use
 * {@link TrayPreferenceActivity#addPreferencesFromIntent} or
 * {@link TrayPreferenceActivity#addPreferencesFromResource(int)}.
 * 
 * @see TrayPreferenceActivity
 */
public class TrayPreferenceManager {
    
    private static final String TAG = "PreferenceManager";

    /**
     * The Activity meta-data key for its XML preference hierarchy.
     */
    public static final String METADATA_KEY_PREFERENCES = "android.preference";
    
    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    
    /**
     * @see #getActivity()
     */
    private Activity mActivity;

    /**
     * Fragment that owns this instance.
     */
    private TrayPreferenceFragment mFragment;

    /**
     * The context to use. This should always be set.
     * 
     * @see #mActivity
     */
    private Context mContext;
    
    /**
     * The counter for unique IDs.
     */
    private long mNextId = 0;

    /**
     * The counter for unique request codes.
     */
    private int mNextRequestCode;

    /**
     * Cached shared preferences.
     */
    private TraySharedPreferences mSharedPreferences;

    /**
     * The TraySharedPreferences name that will be used for all {@link TrayPreference}s
     * managed by this instance.
     */
    private String mSharedPreferencesName;

    /**
     * The TraySharedPreferences mode that will be used for all {@link TrayPreference}s
     * managed by this instance.
     */
    private int mSharedPreferencesMode;

    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;
    private static final int STORAGE_CREDENTIAL_PROTECTED = 2;

    private int mStorage = STORAGE_DEFAULT;

    /**
     * The {@link TrayPreferenceScreen} at the root of the preference hierarchy.
     */
    private TrayPreferenceScreen mPreferenceScreen;

    /**
     * List of activity result listeners.
     */
    private List<OnActivityResultListener> mActivityResultListeners;

    /**
     * List of activity stop listeners.
     */
    private List<OnActivityStopListener> mActivityStopListeners;

    /**
     * List of activity destroy listeners.
     */
    private List<OnActivityDestroyListener> mActivityDestroyListeners;

    /**
     * List of dialogs that should be dismissed when we receive onNewIntent in
     * our PreferenceActivity.
     */
    private List<DialogInterface> mPreferencesScreens;

    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;

    /**
     * @hide
     */
    public TrayPreferenceManager(Activity activity, int firstRequestCode) {
        mActivity = activity;
        mNextRequestCode = firstRequestCode;

        init(activity);
    }

    /**
     * This constructor should ONLY be used when getting default values from
     * an XML preference hierarchy.
     * <p>
     * The {link PreferenceManager#PreferenceManager(Activity)}
     * should be used ANY time a preference will be displayed, since some preference
     * types need an Activity for managed queries.
     */
    public TrayPreferenceManager(Context context) {
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mSharedPreferences = new TraySharedPreferencesImpl(mContext);
        mSharedPreferences.setContext(mContext);
    }

    /**
     * Sets the owning preference fragment
     */
    void setFragment(TrayPreferenceFragment fragment) {
        mFragment = fragment;
    }

    /**
     * Returns the owning preference fragment, if any.
     */
    TrayPreferenceFragment getFragment() {
        return mFragment;
    }

    /**
     * Returns a list of {@link Activity} (indirectly) that match a given
     * {@link Intent}.
     *
     * @param queryIntent The Intent to match.
     * @return The list of {@link ResolveInfo} that point to the matched
     *         activities.
     */
    private List<ResolveInfo> queryIntentActivities(Intent queryIntent) {
        return mContext.getPackageManager().queryIntentActivities(queryIntent,
                PackageManager.GET_META_DATA);
    }

    /**
     * Inflates a preference hierarchy from the preference hierarchies of
     * {@link Activity Activities} that match the given {@link Intent}. An
     * {@link Activity} defines its preference hierarchy with meta-data using
     * the {@link #METADATA_KEY_PREFERENCES} key.
     * <p>
     * If a preference hierarchy is given, the new preference hierarchies will
     * be merged in.
     *
     * @param queryIntent The intent to match activities.
     * @param rootPreferences Optional existing hierarchy to merge the new
     *            hierarchies into.
     * @return The root hierarchy (if one was not provided, the new hierarchy's
     *         root).
     */
    TrayPreferenceScreen inflateFromIntent(Intent queryIntent, TrayPreferenceScreen rootPreferences) {
        final List<ResolveInfo> activities = queryIntentActivities(queryIntent);
        final HashSet<String> inflatedRes = new HashSet<String>();

        for (int i = activities.size() - 1; i >= 0; i--) {
            final ActivityInfo activityInfo = activities.get(i).activityInfo;
            final Bundle metaData = activityInfo.metaData;

            if ((metaData == null) || !metaData.containsKey(METADATA_KEY_PREFERENCES)) {
                continue;
            }

            // Need to concat the package with res ID since the same res ID
            // can be re-used across contexts
            final String uniqueResId = activityInfo.packageName + ":"
                    + activityInfo.metaData.getInt(METADATA_KEY_PREFERENCES);

            if (!inflatedRes.contains(uniqueResId)) {
                inflatedRes.add(uniqueResId);

                final Context context;
                try {
                    context = mContext.createPackageContext(activityInfo.packageName, 0);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Could not create context for " + activityInfo.packageName + ": "
                        + Log.getStackTraceString(e));
                    continue;
                }

                final TrayPreferenceInflater inflater = new TrayPreferenceInflater(context, this);
                final XmlResourceParser parser = activityInfo.loadXmlMetaData(context
                        .getPackageManager(), METADATA_KEY_PREFERENCES);
                rootPreferences = (TrayPreferenceScreen) inflater
                        .inflate(parser, rootPreferences, true);
                parser.close();
            }
        }

        rootPreferences.onAttachedToHierarchy(this);

        return rootPreferences;
    }

    /**
     * Inflates a preference hierarchy from XML. If a preference hierarchy is
     * given, the new preference hierarchies will be merged in.
     *
     * @param context The context of the resource.
     * @param resId The resource ID of the XML to inflate.
     * @param rootPreferences Optional existing hierarchy to merge the new
     *            hierarchies into.
     * @return The root hierarchy (if one was not provided, the new hierarchy's
     *         root).
     * @hide
     */
    public TrayPreferenceScreen inflateFromResource(Context context, @XmlRes int resId,
                                                    TrayPreferenceScreen rootPreferences) {
        final TrayPreferenceInflater inflater = new TrayPreferenceInflater(context, this);
        rootPreferences = (TrayPreferenceScreen) inflater.inflate(resId, rootPreferences, true);
        rootPreferences.onAttachedToHierarchy(this);

        return rootPreferences;
    }

    public TrayPreferenceScreen createPreferenceScreen(Context context) {
        final TrayPreferenceScreen preferenceScreen = new TrayPreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    /**
     * Called by a preference to get a unique ID in its hierarchy.
     *
     * @return A unique ID.
     */
    long getNextId() {
        synchronized (this) {
            return mNextId++;
        }
    }


    /**
     * Explicitly set the storage location used internally by this class to be
     * device-protected storage.
     * <p>
     * On devices with direct boot, data stored in this location is encrypted
     * with a key tied to the physical device, and it can be accessed
     * immediately after the device has booted successfully, both
     * <em>before and after</em> the user has authenticated with their
     * credentials (such as a lock pattern or PIN).
     * <p>
     * Because device-protected data is available without user authentication,
     * you should carefully limit the data you store using this Context. For
     * example, storing sensitive authentication tokens or passwords in the
     * device-protected area is strongly discouraged.
     *
     * see Context#createDeviceProtectedStorageContext()
     */
    public void setStorageDeviceProtected() {
        mStorage = STORAGE_DEVICE_PROTECTED;
        mSharedPreferences = null;
    }

    /** @removed */
    @Deprecated
    public void setStorageDeviceEncrypted() {
        setStorageDeviceProtected();
    }

    /**
     * Explicitly set the storage location used internally by this class to be
     * credential-protected storage. This is the default storage area for apps
     * unless {@code forceDeviceProtectedStorage} was requested.
     * <p>
     * On devices with direct boot, data stored in this location is encrypted
     * with a key tied to user credentials, which can be accessed
     * <em>only after</em> the user has entered their credentials (such as a
     * lock pattern or PIN).
     *
     * see Context#createCredentialProtectedStorageContext()
     * @hide
     */
    @SystemApi
    public void setStorageCredentialProtected() {
        mStorage = STORAGE_CREDENTIAL_PROTECTED;
        mSharedPreferences = null;
    }

    /** @removed */
    @Deprecated
    public void setStorageCredentialEncrypted() {
        setStorageCredentialProtected();
    }

    /**
     * Indicates if the storage location used internally by this class is the
     * default provided by the hosting {@link Context}.
     *
     * @see #setStorageDeviceProtected()
     */
    public boolean isStorageDefault() {
        return mStorage == STORAGE_DEFAULT;
    }

    /**
     * Indicates if the storage location used internally by this class is backed
     * by device-protected storage.
     *
     * @see #setStorageDeviceProtected()
     */
    public boolean isStorageDeviceProtected() {
        return mStorage == STORAGE_DEVICE_PROTECTED;
    }

    /**
     * Indicates if the storage location used internally by this class is backed
     * by credential-protected storage.
     *
     * @see #setStorageDeviceProtected()
     * @hide
     */
    @SystemApi
    public boolean isStorageCredentialProtected() {
        return mStorage == STORAGE_CREDENTIAL_PROTECTED;
    }

    /**
     * Gets a TraySharedPreferences instance that preferences managed by this will
     * use.
     *
     * @return A TraySharedPreferences instance pointing to the file that contains
     *         the values of preferences that are managed by this.
     */
    public TraySharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) {
            mSharedPreferences = newSharedPreferences(mContext);
        }

        return mSharedPreferences;
    }

    /**
     * Create a new TraySharedPreferences instance that preferences managed by this will
     * use.
     *
     * @return A TraySharedPreferences instance pointing to the file that contains
     *         the values of preferences that are managed by this.
     */
    public static TraySharedPreferences newSharedPreferences(Context context) {
        return new TraySharedPreferencesImpl(context);
    }

    /**
     * Gets a TraySharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     *
     * @param context The context of the preferences whose values are wanted.
     * @return A TraySharedPreferences instance that can be used to retrieve and
     *         listen to values of the preferences.
     */
    public static TraySharedPreferences getDefaultSharedPreferences(Context context) {
        // FIXME
        return null;
        //return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
        //        getDefaultSharedPreferencesMode());
    }

    /**
     * Returns the name used for storing default shared preferences.
     *
     * @see #getDefaultSharedPreferences(Context)
     * see Context#getSharedPreferencesPath(String)
     */
    public static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }

    /**
     * Returns the root of the preference hierarchy managed by this class.
     *  
     * @return The {@link TrayPreferenceScreen} object that is at the root of the hierarchy.
     */
    TrayPreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }
    
    /**
     * Sets the root of the preference hierarchy.
     * 
     * @param preferenceScreen The root {@link TrayPreferenceScreen} of the preference hierarchy.
     * @return Whether the {@link TrayPreferenceScreen} given is different than the previous.
     */
    boolean setPreferences(TrayPreferenceScreen preferenceScreen) {
        if (preferenceScreen != mPreferenceScreen) {
            mPreferenceScreen = preferenceScreen;
            return true;
        }
        
        return false;
    }
    
    /**
     * Finds a {@link TrayPreference} based on its key.
     * 
     * @param key The key of the preference to retrieve.
     * @return The {@link TrayPreference} with the key, or null.
     * @see TrayPreferenceGroup#findPreference(CharSequence)
     */
    public TrayPreference findPreference(CharSequence key) {
        if (mPreferenceScreen == null) {
            return null;
        }
        
        return mPreferenceScreen.findPreference(key);
    }

    /**
     * Returns the activity that shows the preferences. This is useful for doing
     * managed queries, but in most cases the use of {@link #getContext()} is
     * preferred.
     * <p>
     * This will return null if this class was instantiated with a Context
     * instead of Activity. For example, when setting the default values.
     * 
     * @return The activity that shows the preferences.
     * @see #mContext
     */
    Activity getActivity() {
        return mActivity;
    }
    
    /**
     * Returns the context. This is preferred over {@link #getActivity()} when
     * possible.
     * 
     * @return The context.
     */
    Context getContext() {
        return mContext;
    }

    /**
     * Registers a listener.
     * 
     * @see OnActivityResultListener
     */
    void registerOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (mActivityResultListeners == null) {
                mActivityResultListeners = new ArrayList<OnActivityResultListener>();
            }
            
            if (!mActivityResultListeners.contains(listener)) {
                mActivityResultListeners.add(listener);
            }
        }
    }

    /**
     * Unregisters a listener.
     * 
     * @see OnActivityResultListener
     */
    void unregisterOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (mActivityResultListeners != null) {
                mActivityResultListeners.remove(listener);
            }
        }
    }

    /**
     * Called by the {@link TrayPreferenceManager} to dispatch a subactivity result.
     */
    void dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        List<OnActivityResultListener> list;
        
        synchronized (this) {
            if (mActivityResultListeners == null) return;
            list = new ArrayList<OnActivityResultListener>(mActivityResultListeners);
        }

        final int N = list.size();
        for (int i = 0; i < N; i++) {
            if (list.get(i).onActivityResult(requestCode, resultCode, data)) {
                break;
            }
        }
    }

    /**
     * Registers a listener.
     * 
     * @see OnActivityStopListener
     * @hide
     */
    public void registerOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (mActivityStopListeners == null) {
                mActivityStopListeners = new ArrayList<OnActivityStopListener>();
            }
            
            if (!mActivityStopListeners.contains(listener)) {
                mActivityStopListeners.add(listener);
            }
        }
    }
    
    /**
     * Unregisters a listener.
     * 
     * @see OnActivityStopListener
     * @hide
     */
    public void unregisterOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (mActivityStopListeners != null) {
                mActivityStopListeners.remove(listener);
            }
        }
    }
    
    /**
     * Called by the {@link TrayPreferenceManager} to dispatch the activity stop
     * event.
     */
    void dispatchActivityStop() {
        List<OnActivityStopListener> list;
        
        synchronized (this) {
            if (mActivityStopListeners == null) return;
            list = new ArrayList<OnActivityStopListener>(mActivityStopListeners);
        }

        final int N = list.size();
        for (int i = 0; i < N; i++) {
            list.get(i).onActivityStop();
        }
    }

    /**
     * Registers a listener.
     * 
     * @see OnActivityDestroyListener
     */
    void registerOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (mActivityDestroyListeners == null) {
                mActivityDestroyListeners = new ArrayList<OnActivityDestroyListener>();
            }

            if (!mActivityDestroyListeners.contains(listener)) {
                mActivityDestroyListeners.add(listener);
            }
        }
    }
    
    /**
     * Unregisters a listener.
     * 
     * @see OnActivityDestroyListener
     */
    void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (mActivityDestroyListeners != null) {
                mActivityDestroyListeners.remove(listener);
            }
        }
    }
    
    /**
     * Called by the {@link TrayPreferenceManager} to dispatch the activity destroy
     * event.
     */
    void dispatchActivityDestroy() {
        List<OnActivityDestroyListener> list = null;
        
        synchronized (this) {
            if (mActivityDestroyListeners != null) {
                list = new ArrayList<OnActivityDestroyListener>(mActivityDestroyListeners);
            }
        }

        if (list != null) {
            final int N = list.size();
            for (int i = 0; i < N; i++) {
                list.get(i).onActivityDestroy();
            }
        }

        // Dismiss any PreferenceScreens still showing
        dismissAllScreens();
    }
    
    /**
     * Returns a request code that is unique for the activity. Each subsequent
     * call to this method should return another unique request code.
     * 
     * @return A unique request code that will never be used by anyone other
     *         than the caller of this method.
     */
    int getNextRequestCode() {
        synchronized (this) {
            return mNextRequestCode++;
        }
    }
    
    void addPreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            
            if (mPreferencesScreens == null) {
                mPreferencesScreens = new ArrayList<DialogInterface>();
            }
            
            mPreferencesScreens.add(screen);
        }
    }
    
    void removePreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            
            if (mPreferencesScreens == null) {
                return;
            }
            
            mPreferencesScreens.remove(screen);
        }
    }
    
    /**
     * Called by {@link TrayPreferenceActivity} to dispatch the new Intent event.
     * 
     * @param intent The new Intent.
     */
    void dispatchNewIntent(Intent intent) {
        dismissAllScreens();
    }

    private void dismissAllScreens() {
        // Remove any of the previously shown preferences screens
        ArrayList<DialogInterface> screensToDismiss;

        synchronized (this) {
            
            if (mPreferencesScreens == null) {
                return;
            }
            
            screensToDismiss = new ArrayList<DialogInterface>(mPreferencesScreens);
            mPreferencesScreens.clear();
        }
        
        for (int i = screensToDismiss.size() - 1; i >= 0; i--) {
            screensToDismiss.get(i).dismiss();
        }
    }
    
    /**
     * Sets the callback to be invoked when a {@link TrayPreference} in the
     * hierarchy rooted at this {@link TrayPreferenceManager} is clicked.
     * 
     * @param listener The callback to be invoked.
     */
    void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        mOnPreferenceTreeClickListener = listener;
    }

    OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return mOnPreferenceTreeClickListener;
    }
    
    /**
     * Interface definition for a callback to be invoked when a
     * {@link TrayPreference} in the hierarchy rooted at this {@link TrayPreferenceScreen} is
     * clicked.
     *
     * @hide
     */
    public interface OnPreferenceTreeClickListener {
        /**
         * Called when a preference in the tree rooted at this
         * {@link TrayPreferenceScreen} has been clicked.
         * 
         * @param preferenceScreen The {@link TrayPreferenceScreen} that the
         *        preference is located in.
         * @param preference The preference that was clicked.
         * @return Whether the click was handled.
         */
        boolean onPreferenceTreeClick(TrayPreferenceScreen preferenceScreen, TrayPreference preference);
    }

    /**
     * Interface definition for a class that will be called when the container's activity
     * receives an activity result.
     */
    public interface OnActivityResultListener {
        
        /**
         * See Activity's onActivityResult.
         * 
         * @return Whether the request code was handled (in which case
         *         subsequent listeners will not be called.
         */
        boolean onActivityResult(int requestCode, int resultCode, Intent data);
    }
    
    /**
     * Interface definition for a class that will be called when the container's activity
     * is stopped.
     */
    public interface OnActivityStopListener {
        
        /**
         * See Activity's onStop.
         */
        void onActivityStop();
    }

    /**
     * Interface definition for a class that will be called when the container's activity
     * is destroyed.
     */
    public interface OnActivityDestroyListener {
        
        /**
         * See Activity's onDestroy.
         */
        void onActivityDestroy();
    }

}
