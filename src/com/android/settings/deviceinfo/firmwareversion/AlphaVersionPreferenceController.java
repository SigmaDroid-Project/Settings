/*
 * Copyright (C) 2019-2023 The LineageOS Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.slices.Sliceable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;

public class AlphaVersionPreferenceController extends BasePreferenceController {

    private static final int DELAY_TIMER_MILLIS = 500;
    private static final int ACTIVITY_TRIGGER_COUNT = 3;
    private static final String ALPHA_BUILD_VERSION = "ro.alpha.modversion";
    private static final String KEY_ALPHA_VERSION = "alpha_version";
    private static final String ALPHA_PACKAGE_TYPE = "ro.alpha.build.package";

    private static final String KEY_LINEAGE_VERSION_PROP = "ro.modversion";
    private static final String KEY_MATRIXX_BUILD_VERSION_PROP = "ro.modversion";
    private static final String KEY_MATRIXX_DEVICE_PROP = "ro.matrixx.device";
    private static final String KEY_MATRIXX_RELEASE_TYPE_PROP = "ro.matrixx.release.type";
    private static final String KEY_MATRIXX_RELEASE_VERSION_PROP = "ro.matrixx.display.version";
    private static final String KEY_MATRIXX_VARIANT_PROP = "ro.matrixx.build.variant";

    private static final String PLATLOGO_PACKAGE_NAME = "com.android.egg";
    private static final String PLATLOGO_ACTIVITY_CLASS =
            PLATLOGO_PACKAGE_NAME + ".EasterEgg";

    private final UserManager mUserManager;
    private final long[] mHits = new long[ACTIVITY_TRIGGER_COUNT];

    private RestrictedLockUtils.EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;
    private boolean fullRomVersion = false;

    public LineageVersionDetailPreferenceController(Context context, String key) {
        super(context, key);
        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        initializeAdminPermissions();
    }

    public AlphaVersionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_ALPHA_VERSION;
    }

    @Override
    public CharSequence getSummary() {
        String version = SystemProperties.get(ALPHA_BUILD_VERSION, "");
        if (TextUtils.isEmpty(version)) return "";
        String packageType = SystemProperties.get(ALPHA_PACKAGE_TYPE, "");
        if (TextUtils.isEmpty(packageType)) return version;
        return version + " (" + packageType + ")";
    }
}
