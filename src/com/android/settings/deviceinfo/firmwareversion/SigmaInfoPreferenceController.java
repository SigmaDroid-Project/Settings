/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2022 Project Arcana
 * Copyright (C) 2022 BananaDroid
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
import android.os.Build;
import android.os.SystemProperties;
import android.widget.TextView;

import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.sigma.settings.utils.SpecUtils;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class SigmaInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_SIGMA_INFO = "sigma_info";

    private static final String PROP_SIGMA_DEVICE = "ro.product.device";
    private static final String KEY_BUILD_STATUS = "os_build_maintainer";

    private static final String KEY_BRAND_NAME_PROP = "ro.product.manufacturer";
    private static final String KEY_DEVICE_NAME_PROP = "org.sigma.device";
    private static final String KEY_MARKET_NAME_PROP = "ro.product.marketname";
    private static final String KEY_SIGMA_BUILD_VERSION_PROP = "org.sigma.build_version";
    private static final String KEY_SIGMA_CODENAME_PROP = "org.sigma.build_codename";
    private static final String KEY_SIGMA_RELEASE_TYPE_PROP = "org.sigma.build_type";
    private static final String KEY_SIGMA_VERSION_PROP = "org.sigma.version.display";
    private static final String PROP_SIGMA_MAINTAINER = "ro.sigma.maintainer";

    private static final String KEY_STORAGE = "storage";
    private static final String KEY_CHIPSET = "chipset";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_DISPLAY = "display";

    private String mBuildStatus;

    final String isOfficial = SystemProperties.get(KEY_SIGMA_RELEASE_TYPE_PROP,
                this.mContext.getString(R.string.device_info_default));

    public SigmaInfoPreferenceController(Context context) {
        super(context);
    }

    private String getSigmaMaintainer() {

        final String SigmaMaintainer = mContext.getResources().getString(R.string.build_maintainer_summary);

        final String buildType = SystemProperties.get(KEY_SIGMA_RELEASE_TYPE_PROP,
                    this.mContext.getString(R.string.device_info_default));
        final String isOffFine = this.mContext.getString(R.string.build_status_summary, SigmaMaintainer);
        final String isOffMiss = this.mContext.getString(R.string.build_status_oopsie);
        final String isCommMiss = this.mContext.getString(R.string.build_status_oopsie);
        final String isCommFine = this.mContext.getString(R.string.build_is_community_summary, SigmaMaintainer);

        if (buildType.toLowerCase().equals("official") && !SigmaMaintainer.equalsIgnoreCase("Unknown")) {
            return isOffFine;
        } else if (buildType.toLowerCase().equals("official") && SigmaMaintainer.equalsIgnoreCase("Unknown")) {
            return isOffMiss;
        } else if (buildType.equalsIgnoreCase("Community") && SigmaMaintainer.equalsIgnoreCase("Unknown")) {
            return isCommMiss;
        } else {
            return isCommFine;
        }
        }

    private String getSigmaBuildStatus() {
	    final String buildType = SystemProperties.get(KEY_SIGMA_RELEASE_TYPE_PROP,
                this.mContext.getString(R.string.device_info_default));
        final String isOfficial = this.mContext.getString(R.string.build_is_official_title);
	    final String isCommunity = this.mContext.getString(R.string.build_is_community_title);
        if (buildType.toLowerCase().equals("official")) {
		return isOfficial;
	    } else {
		return isCommunity;
	    }
    }

    private String getSigmaVersion() {
        String romVersion = SystemProperties.get(KEY_SIGMA_BUILD_VERSION_PROP,
                this.mContext.getString(R.string.device_info_default));
        String romCodename = SystemProperties.get(KEY_SIGMA_CODENAME_PROP,
                this.mContext.getString(R.string.device_info_default));
        String romReleasetype = SystemProperties.get(KEY_SIGMA_RELEASE_TYPE_PROP,
                this.mContext.getString(R.string.device_info_default));

        return romVersion  + " | " + romCodename  + " | " + romReleasetype;
    }

    private String getDeviceName() {
        String deviceBrand = SystemProperties.get(KEY_BRAND_NAME_PROP,
                mContext.getString(R.string.device_info_default));
        String deviceCodename = SystemProperties.get(KEY_DEVICE_NAME_PROP,
                mContext.getString(R.string.device_info_default));
        String deviceName = Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.DEVICE_NAME);

        // Try using market name if there is not set device name
        if (deviceName == null) {
            deviceName = SystemProperties.get(KEY_MARKET_NAME_PROP, null);

            // If market name is not available, fallback to device model
            if (deviceName == null)
                deviceName = Build.MODEL;
        }

        return deviceBrand + " " + deviceName + " (" + deviceCodename + ")";
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference SigmaInfoPreference = screen.findPreference(KEY_SIGMA_INFO);
        final Preference buildStatusPref = screen.findPreference(KEY_BUILD_STATUS);
        final TextView version = (TextView) SigmaInfoPreference.findViewById(R.id.version_message);

        final TextView storText = (TextView) SigmaInfoPreference.findViewById(R.id.cust_storage_summary);
        final TextView battText = (TextView) SigmaInfoPreference.findViewById(R.id.cust_battery_summary);
        final TextView device = (TextView) SigmaInfoPreference.findViewById(R.id.device_message);

        final String isOfficial = SystemProperties.get(KEY_SIGMA_RELEASE_TYPE_PROP,
                this.mContext.getString(R.string.device_info_default));

        final String SigmaDevice = getDeviceName();
        final String SigmaVersion = getSigmaVersion();
        final String SigmaMaintainer = getSigmaMaintainer();
        final String BuildStatus = getSigmaBuildStatus();

        buildStatusPref.setTitle(BuildStatus);
	    buildStatusPref.setSummary(SigmaMaintainer);

        version.setText(SigmaVersion);
        device.setText(SigmaDevice);

        storText.setText(String.valueOf(SpecUtils.getTotalInternalMemorySize()) + "GB ROM + " + SpecUtils.getTotalRAM() + " RAM");
        battText.setText(SpecUtils.getBatteryCapacity(mContext) + " mAh");

        if (isOfficial.toLowerCase().contains("official")) {
		 buildStatusPref.setIcon(R.drawable.verified);
	    } else {
		 buildStatusPref.setIcon(R.drawable.unverified);
	    }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SIGMA_INFO;
    }
}
