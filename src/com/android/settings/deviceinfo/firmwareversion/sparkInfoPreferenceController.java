/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2023 the RisingOS Android Project
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
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class sigmaInfoPreferenceController extends AbstractPreferenceController {

    private static final String KEY_SIGMA_INFO = "sigma_info";
    private static final String KEY_SIGMA_DEVICE = "sigma_device";
    private static final String KEY_SIGMA_VERSION = "sigma_version";
    private static final String KEY_BUILD_STATUS = "rom_build_status";
    private static final String KEY_BUILD_VERSION = "sigma_build_version";

    private static final String PROP_SIGMA_VERSION = "ro.sigma.version";
    private static final String PROP_SIGMA_RELEASETYPE = "ro.sigma.buildtype";
    private static final String PROP_SIGMA_MAINTAINER = "ro.sigma.maintainer";
    private static final String PROP_SIGMA_DEVICE = "ro.sigma.device";
    private static final String PROP_SIGMA_BUILD_VERSION = "ro.sigma.build.version";


    public sigmaInfoPreferenceController(Context context) {
        super(context);
    }

    private String getDeviceName() {
        String device = SystemProperties.get(PROP_SIGMA_DEVICE, "");
        if (device.equals("")) {
            device = Build.MANUFACTURER + " " + Build.MODEL;
        }
        return device;
    }

    private String getSigmaBuildVersion() {
        final String buildVer = SystemProperties.get(PROP_SIGMA_BUILD_VERSION,
                this.mContext.getString(R.string.device_info_default));;

        return buildVer;
    }

    private String getSigmaVersion() {
        final String version = SystemProperties.get(PROP_SIGMA_VERSION,
                this.mContext.getString(R.string.device_info_default));

        return version;
    }

    private String getSigmaReleaseType() {
        final String releaseType = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));

        return releaseType.substring(0, 1).toUpperCase() +
                 releaseType.substring(1).toLowerCase();
    }

    private String getSigmabuildStatus() {
	final String buildType = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String isOfficial = this.mContext.getString(R.string.build_is_official_title);
	final String isCommunity = this.mContext.getString(R.string.build_is_community_title);

	if (buildType.toLowerCase().equals("official")) {
		return isOfficial;
	} else {
		return isCommunity;
	}
    }

    private String getSigmaMaintainer() {
	final String SigmaMaintainer = SystemProperties.get(PROP_SIGMA_MAINTAINER,
                this.mContext.getString(R.string.device_info_default));
	final String buildType = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
        final String isOffFine = this.mContext.getString(R.string.build_is_official_summary, SigmaMaintainer);
	final String isOffMiss = this.mContext.getString(R.string.build_is_official_summary_oopsie);
	final String isCommMiss = this.mContext.getString(R.string.build_is_community_summary_oopsie);
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

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference arcVerPref = screen.findPreference(KEY_SIGMA_VERSION);
        final Preference arcDevPref = screen.findPreference(KEY_SIGMA_DEVICE);
        final Preference buildStatusPref = screen.findPreference(KEY_BUILD_STATUS);
        final Preference buildVerPref = screen.findPreference(KEY_BUILD_VERSION);
        final String SigmaVersion = getSigmaVersion();
        final String SigmaDevice = getDeviceName();
        final String SigmaReleaseType = getSigmaReleaseType();
        final String SigmaMaintainer = getSigmaMaintainer();
	final String buildStatus = getSigmabuildStatus();
	final String buildVer = getSigmaBuildVersion();
	final String isOfficial = SystemProperties.get(PROP_SIGMA_RELEASETYPE,
                this.mContext.getString(R.string.device_info_default));
	buildStatusPref.setTitle(buildStatus);
	buildStatusPref.setSummary(SigmaMaintainer);
	buildVerPref.setSummary(buildVer);
        arcVerPref.setSummary(SigmaVersion);
        arcDevPref.setSummary(SigmaDevice);
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
