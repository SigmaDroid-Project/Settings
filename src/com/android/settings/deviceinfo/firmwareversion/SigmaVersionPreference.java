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

public class SigmaVersionPreference extends ExpandablePreference {

    private static final String SIGMA_BUILD_VERSION = "ro.sigma.modversion";
    private static final String SIGMA_PACKAGE_TYPE = "ro.sigma.build.package";
    private static final String SIGMA_VERSION = "ro.sigma.display.version";

    public SigmaVersionPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setCollapsedSummary(getShortSigmaVersion(context));
        setExpandedSummary(getFullSigmaVersion(context));
    }

    public SigmaVersionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SigmaVersionPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
    }

    public SigmaVersionPreference(Context context) {
        this(context, null);
    }

    private String getFullSigmaVersion(Context context) {
        return SystemProperties.get(SIGMA_VERSION,
                context.getString(R.string.unknown));
    }

    private String getShortSigmaVersion(Context context) {
        String romVersion = SystemProperties.get(SIGMA_BUILD_VERSION,
                context.getString(R.string.device_info_default));
        String romPackagetype = SystemProperties.get(SIGMA_PACKAGE_TYPE,
                context.getString(R.string.device_info_default));
        String shortVersion = romVersion + " (" + romPackagetype + ")";
        return shortVersion;
    }
}
