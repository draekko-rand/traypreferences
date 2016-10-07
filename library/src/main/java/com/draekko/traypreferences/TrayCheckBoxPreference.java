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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

/**
 * A {@link TrayPreference} that provides checkbox widget
 * functionality.
 * <p>
 * This preference will store a boolean into the TraySharedPreferences.
 * 
 * @attr ref R.styleable#CheckBoxPreference_summaryOff
 * @attr ref R.styleable#CheckBoxPreference_summaryOn
 * @attr ref R.styleable#CheckBoxPreference_disableDependentsState
 */
public class TrayCheckBoxPreference extends TrayTwoStatePreference {

    public TrayCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TrayCheckBoxPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TrayCheckBoxPreferences, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.TrayCheckBoxPreferences_summaryOn));
        setSummaryOff(a.getString(R.styleable.TrayCheckBoxPreferences_summaryOff));
        setDisableDependentsState(a.getBoolean(
                R.styleable.TrayCheckBoxPreferences_disableDependentsState, false));
        a.recycle();
    }

    public TrayCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.trayCheckBoxPreferenceStyle);
    }

    public TrayCheckBoxPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View checkboxView = view.findViewById(R.id.checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
            ((Checkable) checkboxView).setChecked(mChecked);
        }

        syncSummaryView(view);
    }
}
