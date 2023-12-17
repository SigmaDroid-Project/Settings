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

import static com.android.settings.network.InternetUpdater.INTERNET_WIFI;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.network.InternetUpdater;
import com.android.settings.widget.SummaryUpdater;
import com.android.settings.wifi.WifiSummaryUpdater;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.utils.ThreadUtils;

/**
 * PreferenceController to update the wifi summary.
 */
public class NadWifiHomepagePreferenceController extends AbstractPreferenceController implements
        LifecycleObserver, SummaryUpdater.OnSummaryChangeListener,
        InternetUpdater.InternetChangeListener {

    public static final String KEY = "main_toggle_wifi";

    private Preference mPreference;
    private final WifiSummaryUpdater mSummaryHelper;
    private InternetUpdater mInternetUpdater;
    private @InternetUpdater.InternetType int mInternetType;

    public NadWifiHomepagePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle == null) {
            throw new IllegalArgumentException("Lifecycle must be set");
        }
        mSummaryHelper = new WifiSummaryUpdater(mContext, this);
        mInternetUpdater = new InternetUpdater(context, lifecycle, this);
        mInternetType = mInternetUpdater.getInternetType();
        lifecycle.addObserver(this);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(KEY);
    }

    @Override
    public void updateState(Preference preference) {
        if (mPreference == null) {
            return;
        }
        mPreference.setSummary(mSummaryHelper.getSummary());
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
        mSummaryHelper.register(true);
    }

    /** @OnLifecycleEvent(ON_PAUSE) */
    @OnLifecycleEvent(ON_PAUSE)
    public void onPause() {
        mSummaryHelper.register(false);
    }

    /**
     * Called when internet type is changed.
     *
     * @param internetType the internet type
     */
    @Override
    public void onInternetTypeChanged(@InternetUpdater.InternetType int internetType) {
        final boolean needUpdate = (internetType != mInternetType);
        mInternetType = internetType;
        if (needUpdate) {
            ThreadUtils.postOnMainThread(() -> {
                updateState(mPreference);
            });
        }
    }

    /**
     * Called when airplane mode state is changed.
     */
    @Override
    public void onAirplaneModeChanged(boolean isAirplaneModeOn) {
        ThreadUtils.postOnMainThread(() -> {
            updateState(mPreference);
        });
    }

    @Override
    public void onSummaryChanged(String summary) {
        if (mInternetType == INTERNET_WIFI && mPreference != null) {
            mPreference.setSummary(summary);
        }
    }
}