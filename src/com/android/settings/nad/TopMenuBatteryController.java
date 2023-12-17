/*
 * Copyright (C) 2022 The Nusantara Project
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

package com.android.settings.nad;

import static androidx.lifecycle.Lifecycle.Event.ON_PAUSE;
import static androidx.lifecycle.Lifecycle.Event.ON_RESUME;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.fuelgauge.BatteryBroadcastReceiver;
import com.android.settings.nad.TopMenuBattery;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * PreferenceController to update the wifi summary.
 */
public class TopMenuBatteryController extends AbstractPreferenceController implements
        LifecycleObserver {

    public static final String KEY = "top_level_nad_battery";

    private TopMenuBattery mPreference;
    private final BatteryBroadcastReceiver mBatteryBroadcastReceiver;

    public TopMenuBatteryController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle == null) {
            throw new IllegalArgumentException("Lifecycle must be set");
        }
        lifecycle.addObserver(this);
        mBatteryBroadcastReceiver = new BatteryBroadcastReceiver(context);
        mBatteryBroadcastReceiver.setBatteryChangedListener(type -> {
        	if (mPreference != null) {
        	    mPreference.quickUpdateBattery();
        	}
        });
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(KEY);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    /** @OnLifecycleEvent(ON_RESUME) */
    @OnLifecycleEvent(ON_RESUME)
    public void onResume() {
        mBatteryBroadcastReceiver.register();
    }

    /** @OnLifecycleEvent(ON_PAUSE) */
    @OnLifecycleEvent(ON_PAUSE)
    public void onPause() {
        mBatteryBroadcastReceiver.unRegister();
    }
}