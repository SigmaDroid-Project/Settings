/*
 * Copyright (C) 2016-2020 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.preferences.ExpandablePreference;
import com.android.settings.R;

public class BetaVersionPreference extends ExpandablePreference {

    private static final String BETA_BUILD_VERSION = "ro.beta.modversion";
    private static final String BETA_PACKAGE_TYPE = "ro.beta.build.package";
    private static final String BETA_VERSION = "ro.beta.display.version";

    public BetaVersionPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setCollapsedSummary(getShortBetaVersion(context));
        setExpandedSummary(getFullBetaVersion(context));
    }

    public BetaVersionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BetaVersionPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
    }

    public BetaVersionPreference(Context context) {
        this(context, null);
    }

    private String getFullBetaVersion(Context context) {
        return SystemProperties.get(BETA_VERSION,
                context.getString(R.string.unknown));
    }

    private String getShortBetaVersion(Context context) {
        String romVersion = SystemProperties.get(BETA_BUILD_VERSION,
                context.getString(R.string.device_info_default));
        String romPackagetype = SystemProperties.get(BETA_PACKAGE_TYPE,
                context.getString(R.string.device_info_default));
        String shortVersion = romVersion + " (" + romPackagetype + ")";
        return shortVersion;
    }
}
